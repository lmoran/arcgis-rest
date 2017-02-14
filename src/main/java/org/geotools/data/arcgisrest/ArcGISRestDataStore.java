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
import java.util.logging.Level;

import org.geotools.data.Query;
import org.geotools.data.arcgisrest.schema.catalog.Catalog;
import org.geotools.data.arcgisrest.schema.catalog.Dataset;
import org.geotools.data.arcgisrest.schema.webservice.Webservice;
import org.geotools.data.arcgisrest.schema.catalog.Error_;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.opengis.feature.type.Name;
import org.geotools.feature.NameImpl;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.URI;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import sun.misc.IOUtils;

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
    String response = null;
    Error_ err;
    try {
      response = ArcGISRestDataStore.InputStreamToString(
          this.retrieveJSON("GET", apiUrl, DEFAULT_PARAMS));
      this.catalog = (new Gson()).fromJson(response, Catalog.class);
    } catch (JsonSyntaxException | IOException e) {
      // Checks whether we have an AercGIS error message
      err = (new Gson()).fromJson(response, Error_.class);
      LOGGER.log(Level.SEVERE,
          "JSON syntax error " + err.getCode() + " " + err.getMessage(), e);
      throw (e);
    }

    // Sets the list of the datasets referenced in the catalog
    this.entries.clear();
    this.datasets.clear();
    if (this.catalog.getDataset() != null) {
      this.catalog.getDataset().forEach((ds) -> {
        Webservice ws = null;
        InputStream responseWs = null;
        String responseWSString = null;
        try {
          responseWSString = ArcGISRestDataStore.InputStreamToString(
              this.retrieveJSON("GET", new URL(ds.getWebService().toString()),
                  ArcGISRestDataStore.DEFAULT_PARAMS));
          ws = (new Gson()).fromJson(responseWSString, Webservice.class);
        } catch (JsonSyntaxException | IOException e) {
          // Checks whether we have an ArcGIS error message
          Error_ errWS = (new Gson()).fromJson(responseWSString, Error_.class);
          LOGGER.log(Level.SEVERE,
              "Error during retrieval of dataset " + ds.getWebService() + " "
                  + errWS.getCode() + " " + errWS.getMessage(),
              e);
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

    HttpClient client = new HttpClient();
    
    // Instanties the method based on the methType parameter
    HttpMethodBase meth;
    if (methType.equals("GET")) {
      meth = new GetMethod();
    } else {
      meth = new PostMethod();
    }

    // Sets the URI, request parameters and request body (depending on mthod type) 
    URI uri = new URI(url.toString(), false);
    NameValuePair[] kvps = new NameValuePair[params.size()];
    int i = 0;
    for (Object entry : params.entrySet().toArray()) {
      kvps[i++] = new NameValuePair(((Map.Entry) entry).getKey().toString(),
         ((Map.Entry) entry).getValue().toString());
    }

    if (methType.equals("GET")) {
      meth.setQueryString(kvps);
      uri.setQuery(meth.getQueryString());
    } else {
      ((PostMethod) (meth)).setContentChunked(true);
      ((PostMethod) (meth)).setRequestBody(kvps);
    }

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
        throw new IOException("HTTP Error: " + status + " URL");
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
