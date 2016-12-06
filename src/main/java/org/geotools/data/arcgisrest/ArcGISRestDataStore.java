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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.geotools.data.Query;
import org.geotools.data.arcgisrest.schema.catalog.Catalog;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.referencing.FactoryException;
import org.geotools.feature.NameImpl;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScheme;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import com.google.gson.Gson;

import sun.net.www.protocol.http.HttpURLConnection;

public class ArcGISRestDataStore extends ContentDataStore {

  // Common paramterse used n the API, together with default values
  public static HttpMethodParams defaultParams = new HttpMethodParams();
  public static final String GEOMETRYTYPE_PARAM = "geometryType";
  public static final String GEOMETRY_PARAM = "geometry";
  public static final String COUNT_PARAM = "returnCountOnly";
  public static final String FORMAT_PARAM = "f";
  public static final String ATTRIBUTES_PARAM = "outFields";
  public static final String WITHGEOMETRY_PARAM = "returnGeometry";
  public static final String SRS_PARAM = "spatialRel";

  static {
    defaultParams.setParameter(FORMAT_PARAM, "json");
    defaultParams.setParameter(WITHGEOMETRY_PARAM, "true");
    defaultParams.setParameter(GEOMETRYTYPE_PARAM, "esriGeometryEnvelope");
  }

  // FIXME: can be made to work for both ArcGIS online and ArcGIS ReST API
  // proper?
  // AFAIK, Arc online retuns a data.json document tha contains the ArcGIS ReST
  // API endpoints proper, which is different from teh ArcGIS ReST API
  protected URL namespace;
  protected URL apiUrl;
  protected String user;
  protected String password;
  private Catalog catalog;

  public ArcGISRestDataStore(String namespace, String apiEndpoint, String user,
      String password) throws MalformedURLException {

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
  public String retrieveJSON(URL url, HttpMethodParams params)
      throws IOException {

    // Creates the HTTP client and set the request parameters (the one passed to
    // the methed and the default ones)
    HttpClient client = new HttpClient();
    HttpMethod method = new GetMethod(url.toString());
    method.setParams(defaultParams);
    method.setParams(params);
    method.setFollowRedirects(false);

    // Adds authorization if login/password is set
    // FIXME: not quite sure null is passed when the field is left empty, better
    // check
    if (this.user != null && this.password != null) {
      method.addRequestHeader("Authentication",
          (new UsernamePasswordCredentials(user, password)).toString());
    }

    // Re-tries the request if necessary
    while (true) {

      // Executes the request
      int status = client.executeMethod(method);

      // If HTTP error, throws an exception
      if (status != HttpStatus.SC_OK) {
        throw new IOException("HTTP Error: " + status + " " + url.toString());
      }

      // Retrieve the wait period is returned by the server
      int wait = 0;
      Header header = method.getResponseHeader("Retry-After");
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

    // Extracts the response
    String json = method.getResponseBodyAsString();
    method.releaseConnection();

    // Checks the return JSON for error (yes, ESRI thinks a good idea to return
    // errors with 200 error codes)
    org.geotools.data.arcgisrest.schema.catalog.Error err = (new Gson())
        .fromJson(json,
            org.geotools.data.arcgisrest.schema.catalog.Error.class);
    if (err != null && err.getError() != null
        && err.getError().getCode() != null
        && err.getError().getCode() != HttpURLConnection.HTTP_OK) {
      throw new IOException(
          "ArcGIS ReST API Error : " + err.getError().getCode() + " "
              + err.getError().getMessage() + " URL:" + url.toString());
    }

    // Returns the JSON response
    return json;
  }

  /**
   * Returns the datastore catalog
   * 
   * @return Catalog
   */
  public Catalog getCatalog() {
    return this.catalog;
  }

  @Override
  protected List<Name> createTypeNames() throws IOException {

    List<Name> datasets = new ArrayList<Name>();

    // Retrieves the catalog JSON document
    this.catalog = (new Gson()).fromJson(
        this.retrieveJSON(apiUrl, new HttpMethodParams()), Catalog.class);

    // Returns the list of datasets referenced in the catalog
    this.entries.clear();
    if (this.catalog.getDataset() != null) {
      this.catalog.getDataset().forEach((ds) -> {
        Name dsName = new NameImpl(namespace.toExternalForm(), ds.getTitle());
        datasets.add(dsName);
        this.entries.put(dsName, new ContentEntry(this, dsName));
      });
    }

    return datasets;
  }

  @Override
  protected ContentFeatureSource createFeatureSource(ContentEntry entry)
      throws IOException {

    try {
      return new ArcGISRestFeatureSource(entry, new Query());
    } catch (URISyntaxException | FactoryException e) {
      throw new IOException(e.getMessage());
    }
  }

  public URL getNamespace() {
    return namespace;
  }

  // TODO: ?
  @Override
  public void dispose() {
    super.dispose();
  }

}
