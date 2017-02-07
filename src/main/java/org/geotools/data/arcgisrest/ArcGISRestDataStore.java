/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2016, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */

package org.geotools.data.arcgisrest;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.logging.Level;

import javax.xml.ws.http.HTTPException;

import org.geotools.data.Query;
import org.geotools.data.arcgisrest.schema.catalog.Catalog;
import org.geotools.data.arcgisrest.schema.catalog.Dataset;
import org.geotools.data.arcgisrest.schema.webservice.Webservice;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.opengis.feature.type.Name;
import org.geotools.feature.NameImpl;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.URI;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import sun.misc.IOUtils;
import sun.net.www.protocol.http.HttpURLConnection;

public class ArcGISRestDataStore extends ContentDataStore {

  // Common parameters used in the API
  public static final String GEOMETRYTYPE_PARAM = "geometryType";
  public static final String GEOMETRY_PARAM = "geometry";
  public static final String COUNT_PARAM = "returnCountOnly";
  public static final String FORMAT_PARAM = "f";
  public static final String ATTRIBUTES_PARAM = "outFields";
  public static final String WITHGEOMETRY_PARAM = "returnGeometry";

  // Parameter values
  public static final String FORMAT_JSON = "json";
  public static final String FORMAT_GEOJSON = "geojson";

  // Default request parameter values
  public static Map<String, Object> DEFAULT_PARAMS = new HashMap<String, Object>();

  static {
    DEFAULT_PARAMS.put(FORMAT_PARAM, FORMAT_JSON);
    DEFAULT_PARAMS.put(WITHGEOMETRY_PARAM, "true");
    DEFAULT_PARAMS.put(GEOMETRYTYPE_PARAM, "esriGeometryEnvelope");
  }

  // Cache of feature sources
  protected Map<Name, ArcGISRestFeatureSource> featureSources = new HashMap<Name, ArcGISRestFeatureSource>();

  // Default feature type geometry attribute
  public static final String GEOMETRY_ATTR = "geometry";

  // FIXME: can be made to work for both ArcGIS online and ArcGIS ReST API
  // proper?
  // AFAIK, Arc online retuns a data.json document tha contains the ArcGIS ReST
  // API endpoints proper, which is different from teh ArcGIS ReST API
  protected URL namespace;
  protected URL apiUrl;
  protected String user;
  protected String password;
  protected Catalog catalog;
  protected Map<Name, Dataset> datasets = new HashMap<Name, Dataset>();

  public ArcGISRestDataStore(String namespace, String apiEndpoint, String user,
      String password)
      throws MalformedURLException, JsonSyntaxException, IOException {

    super();

    try {
      this.namespace = new URL(namespace);
    } catch (MalformedURLException e) {
      LOGGER.log(Level.SEVERE,
          "Namespace \"" + namespace + "\" is not properly formatted", e);
      throw (e);
    }
    try {
      this.apiUrl = new URL(apiEndpoint);
    } catch (MalformedURLException e) {
      LOGGER.log(Level.SEVERE,
          "URL \"" + apiEndpoint + "\" is not properly formatted", e);
      throw (e);
    }
    this.user = user;
    this.password = password;

    // Retrieves the catalog JSON document
    InputStream response = null;
    try {
      response = this.retrieveJSON("GET", apiUrl, DEFAULT_PARAMS);
      this.catalog = (new Gson()).fromJson(
          ArcGISRestDataStore.InputStreamToString(response), Catalog.class);
    } catch (JsonSyntaxException | IOException e) {
      LOGGER.log(Level.SEVERE, "JSON syntax error " + e.getMessage(), e);
      throw (e);
    }

    // Sets the list of the datasets referenced in the catalog
    this.entries.clear();
    this.datasets.clear();
    if (this.catalog.getDataset() != null) {
      this.catalog.getDataset().forEach((ds) -> {
        Webservice ws = null;
        InputStream responseWs = null;
        try {
          ws = (new Gson()).fromJson(
              ArcGISRestDataStore.InputStreamToString(this.retrieveJSON("GET",
                  new URL(ds.getWebService().toString()),
                  ArcGISRestDataStore.DEFAULT_PARAMS)),
              Webservice.class);
        } catch (JsonSyntaxException | IOException e) {
          LOGGER.log(Level.SEVERE,
              "Error during retrieval of dataset " + ds.getWebService(), e);
        }

        Name dsName = new NameImpl(namespace, ws.getName());
        ContentEntry entry = new ContentEntry(this, dsName);
        this.datasets.put(dsName, ds);
        this.entries.put(dsName, entry);
      });
    }

  }

  /**
   * Returns the datastore catalog
   * 
   * @return Catalog
   */
  public Catalog getCatalog() {
    return this.catalog;
  }

  /**
   * Returns the ArcGIS ReST API dataset given its name
   * 
   * @param name
   *          Dataset name
   * @return Dataset
   */
  public Dataset getDataset(Name name) {
    return this.datasets.get(name);
  }

  @Override
  protected List<Name> createTypeNames() {
    List<Name> typeNames = new ArrayList<Name>();
    Iterator<ContentEntry> iter = this.entries.values().iterator();
    while (iter.hasNext()) {
      typeNames.add(iter.next().getName());
    }

    return typeNames;
  }

  @Override
  protected ContentFeatureSource createFeatureSource(ContentEntry entry)
      throws IOException {

    ArcGISRestFeatureSource featureSource = this.featureSources
        .get(entry.getName());
    if (featureSource == null) {
      featureSource = new ArcGISRestFeatureSource(entry, new Query());
      this.featureSources.put(entry.getName(), featureSource);
    }

    return featureSource;
  }

  public URL getNamespace() {
    return namespace;
  }

  // TODO: ?
  @Override
  public void dispose() {
    super.dispose();
  }

  /**
   * Helper method returning a JSON String out of a resource belongining to a
   * ArcGIS ReST API instance (via a GET). If present, it sends authorixzation.
   * 
   * @param url
   *          The endpoint of the resource
   * @param params
   *          Request parameters
   * @return A string representing the JSON, null
   * @throws IOException
   * @throws InterruptedException
   */
  public InputStream retrieveJSON(String methType, URL url,
      Map<String, Object> params) throws IOException {

    // Creates the HTTP client and set the request parameters (the one passed to
    // the methed and the default ones)
    HttpClient client = new HttpClient();

    URI uri = new URI(url.toString(), false);

    HttpMethodBase meth;
    if (methType.equals("GET")) {
      meth = new GetMethod();
    } else {
      meth = new PostMethod();
    }

    // FIXME: there must be a better way... possibly with
    // https://hc.apache.org/httpclient-3.x/apidocs/org/apache/commons/httpclient/NameValuePair.html
    StringJoiner joiner = new StringJoiner("&");
    params.forEach((key, value) -> {
      joiner.add(key + "=" + value.toString());
    });

    if (methType.equals("GET")) {
      uri.setQuery(joiner.toString());
    } else {
      ((PostMethod) (meth)).setRequestEntity(new StringRequestEntity(
          joiner.toString(), "application/x-www-form-urlencoded", null));
    }

    // postMeth.setFollowRedirects(true);
    meth.setURI(uri);

    // Adds authorization if login/password is set
    if (this.user != null && this.password != null) {
      meth.addRequestHeader("Authentication",
          (new UsernamePasswordCredentials(user, password)).toString());
    }

    // Re-tries the request if necessary
    while (true) {

      // Executes the request (a POST, since the URL may get too long)
      int status = client.executeMethod(meth);

      // If HTTP error, throws an exception
      if (status != HttpStatus.SC_OK) {
        throw new IOException("HTTP Error: " + status + " URL: "
            + url.toString() + " BODY: " + joiner.toString());
      }

      // Retrieve the wait period is returned by the server
      int wait = 0;
      Header header = meth.getResponseHeader("Retry-After");
      if (header != null) {
        wait = Integer.valueOf(header.getValue());
      }

      // Exists if no retry is necessary
      if (wait == 0) {
        break;
      }

      try {
        Thread.sleep(wait * 1000);
      } catch (InterruptedException e) {
        LOGGER.log(Level.SEVERE, "InterruptedException: " + e.getMessage());
        throw new IOException(e);
      }
    }

    // Extracts an returns the response
    return meth.getResponseBodyAsStream();

    // Checks the return JSON for error (yes, ESRI thinks a good idea to return
    // errors with 200 error codes)
    // FIXME: this should be moved to where retrieveJSON is called, so that
    // where the parsing into an object fails, the checking for an erro rmessage
    // is performed
    /*
    org.geotools.data.arcgisrest.schema.catalog.Error err = (new Gson())
        .fromJson(ArcGISRestDataStore.InputStreamToString(response),
            org.geotools.data.arcgisrest.schema.catalog.Error.class);
    meth.releaseConnection();
    if (err != null && err.getError() != null
        && err.getError().getCode() != null
        && err.getError().getCode() != HttpURLConnection.HTTP_OK) {
      throw new IOException("ArcGIS ReST API Error : "
          + err.getError().getCode() + " " + err.getError().getMessage() + " "
          + err.getError().getDetails() + " URL:" + url.toString());
    }
*/
  }

  /**
   * Helper method to convert an entire InputStream to a String and close the
   * steeam
   * 
   * @param response
   *          input stream to convert to a String
   * @returns the converted String
   * @throws IOException
   */
  public static String InputStreamToString(InputStream istream)
      throws IOException {
    String s = new String(IOUtils.readFully(istream, -1, true));
    istream.close();
    return s;
  }

}
