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

package org.geotools.arcgisrest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;

import org.geotools.arcgisrest.schema.catalog.Catalog;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.opengis.feature.type.Name;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.resource.ClientResource;

public class ArcGISRestDataStore extends ContentDataStore {

  // FIXME: can be made to work for both ArcGIS online and ArcGIS ReST API
  // proper?
  // AFAIK, Arc online retuns a data.json document tha contains the ArcGIS ReST
  // API endpoints proper, which is different from teh ArcGIS ReST API
  protected String namespace;
  protected URL apiUrl;
  protected String user;
  protected String password;

  public ArcGISRestDataStore(String namespace, String apiEndpoint, String user,
      String password) throws IOException {
    super();
    try {
      this.apiUrl = new URL(apiEndpoint);
    } catch (MalformedURLException e) {
      LOGGER.log(Level.SEVERE,
          "URL \"" + apiEndpoint + "\" is not properly formatted", e);
      throw (e);
    }
    this.namespace = namespace;
    this.user = user;
    this.password = password;
  }

  @Override
  protected List<Name> createTypeNames() throws IOException {

    // Colnnects to the API
    Client client = new Client(new Context(), Protocol.HTTP);
    ClientResource resource = new ClientResource(this.apiUrl.toString());
    resource.setMethod(Method.GET);
    resource.setNext(client);

    // Adds authorization if login/password is set
    // FIXME: not quite sure null is passed when the field is left empty, better
    // check
    if (this.user == null && this.password == null) {
      resource.setChallengeResponse(ChallengeScheme.HTTP_BASIC, this.user,
          this.password);
    }

    Catalog catalog= resource.wrap(Catalog.class);

    /**
     * TODO: Parses JSON document according to this schema
     * https://project-open-data.cio.gov/v1.1/schema/catalog.json
     */
    return null;
  }

  @Override
  protected ContentFeatureSource createFeatureSource(ContentEntry entry)
      throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

}
