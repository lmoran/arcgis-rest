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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.restlet.resource.ResourceException;

import org.geotools.data.Query;
import org.geotools.data.arcgisrest.schema.catalog.Catalog;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.opengis.feature.type.Name;
import org.opengis.referencing.FactoryException;
import org.geotools.feature.NameImpl;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.resource.ClientResource;

import com.google.gson.Gson;

import sun.net.www.protocol.http.HttpURLConnection;

public class ArcGISRestDataStore extends ContentDataStore {

  protected static final Parameter jsonParam = new Parameter("f", "json");

  // FIXME: can be made to work for both ArcGIS online and ArcGIS ReST API
  // proper?
  // AFAIK, Arc online retuns a data.json document tha contains the ArcGIS ReST
  // API endpoints proper, which is different from teh ArcGIS ReST API
  protected URL namespace;
  protected URL apiUrl;
  protected String user;
  protected String password;
  protected Catalog catalog;

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
   * Helper method returning a String out of an URL point to the ArcGIS ReST API
   * 
   * @param resUrl
   *          The endpoint of the resource
   * @return A string representing the JSON, null
   * @throws IOException
   */
  public String retrieveJSON(URL resUrl) throws IOException, ResourceException {

    Client client = new Client(new Context(), Protocol.HTTP);
    ClientResource resource = new ClientResource(Method.GET, resUrl.toString());

    resource.addQueryParameter(jsonParam);
    resource.setNext(client);

    // Adds authorization if login/password is set
    // FIXME: not quite sure null is passed when the field is left empty, better
    // check
    if (this.user != null && this.password != null) {
      resource.setChallengeResponse(ChallengeScheme.HTTP_BASIC, this.user,
          this.password);
    }

    resource.get(MediaType.APPLICATION_JSON);

    // In case of HTTP errors, throws an exceptoin
    if (resource.getStatus().getCode() != HttpURLConnection.HTTP_OK) {
      throw new ResourceException(resource.getStatus().getCode(), "", "",
          resUrl.toString());
    }

    // Parses JSON document according to this schema
    String json = resource.getResponseEntity().getText();

    // Checks the return JSON for error (yes, ESRI thinks a good idea to return
    // errors with 200 error codes)
    org.geotools.data.arcgisrest.schema.catalog.Error err = (new Gson())
        .fromJson(json,
            org.geotools.data.arcgisrest.schema.catalog.Error.class);
    if (err != null && err.getError() != null
        && err.getError().getCode() != null
        && err.getError().getCode() != HttpURLConnection.HTTP_OK) {
      throw new ResourceException(err.getError().getCode(), "",
          err.getError().getMessage(), resUrl.toString());
    }

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
    try {
      this.catalog = (new Gson()).fromJson(this.retrieveJSON(apiUrl),
          Catalog.class);
    } catch (ResourceException e) {
      String msg = e.getStatus().getUri() + ": " + e.getStatus().getCode() + " "
          + e.getStatus().getDescription();
      LOGGER.log(Level.SEVERE, msg);
      throw new IOException(msg);
    }

    // Returns the list of datasets referenced in the catalog
    if (this.catalog.getDataset() != null) {
      this.catalog.getDataset().forEach((ds) -> {
        datasets.add(new NameImpl(namespace.toExternalForm(), ds.getTitle()));
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

}
