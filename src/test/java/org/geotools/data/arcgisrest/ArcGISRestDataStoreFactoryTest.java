/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008 - 2016, Open Source Geospatial Foundation (OSGeo)
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
 */
package org.geotools.data.arcgisrest;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.DataStore;
import org.geotools.data.arcgisrest.ArcGISRestDataStoreFactory;
import org.geotools.util.logging.Logging;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

import static org.mockito.Mockito.*;

/**
 * @source $URL$
 */
public class ArcGISRestDataStoreFactoryTest {

  private static final Logger LOGGER = Logging
      .getLogger("org.geotools.data.arcgisrest");

  public static String URL = "http://data.dhs.opendata.arcgis.com/data.json";
  public static String NAMESPACE = "http://aurin.org.au";
  public static String USER = "testuser";
  public static String PASSWORD = "testpassword";

  private ArcGISRestDataStoreFactory dsf;
  private Map<String, Serializable> params;

  @Before
  public void setUp() throws Exception {
    dsf = new ArcGISRestDataStoreFactory();
    params = new HashMap<String, Serializable>();
  }

  @After
  public void tearDown() throws Exception {
    dsf = null;
    params = null;
  }

  private DataStore testCreateDataStore(final String namespace,
      final String url, final String user, final String password)
      throws IOException {

    params.put(ArcGISRestDataStoreFactory.NAMESPACE_PARAM.key, namespace);
    params.put(ArcGISRestDataStoreFactory.URL_PARAM.key, url);
    params.put(ArcGISRestDataStoreFactory.USER_PARAM.key, user);
    params.put(ArcGISRestDataStoreFactory.PASSWORD_PARAM.key, password);
    (new ArcGISRestDataStoreFactory()).createDataStore(params);
    return (new ArcGISRestDataStoreFactory()).createNewDataStore(params);
  }

  @Test
  public void testCanProcess() {
    // Nothing set
    assertFalse(dsf.canProcess(params));

    // Namespace set
    params.put(ArcGISRestDataStoreFactory.NAMESPACE_PARAM.key, NAMESPACE);
    assertFalse(dsf.canProcess(params));

    // URL set wrongly
    params.put(ArcGISRestDataStoreFactory.URL_PARAM.key, "ftp://example.com");
    assertTrue(dsf.canProcess(params));

    // URL set
    params.put(ArcGISRestDataStoreFactory.URL_PARAM.key, URL);
    assertTrue(dsf.canProcess(params));

    // Username set
    params.put(ArcGISRestDataStoreFactory.USER_PARAM.key, USER);
    assertTrue(dsf.canProcess(params));

    // Password set
    params.put(ArcGISRestDataStoreFactory.PASSWORD_PARAM.key, PASSWORD);
    assertTrue(dsf.canProcess(params));
  }

  @Test(expected = MalformedURLException.class)
  public void testCreateDataStoreMalformedNamespace() throws IOException {
    LOGGER.setLevel(Level.OFF);
    testCreateDataStore("aaa", "bbb", "ccc", "ddd");
    LOGGER.setLevel(Level.FINEST);
  }

  @Test(expected = MalformedURLException.class)
  public void testCreateDataStoreMalformedURL() throws IOException {
    LOGGER.setLevel(Level.OFF);
    testCreateDataStore(NAMESPACE, "bbb", "ccc", "ddd");
    LOGGER.setLevel(Level.FINEST);
  }

  @Test
  public void testCreateDataStoreFromCatalogJSON() throws IOException {

    Response response = mock(Response.class);
    Representation entity = mock(Representation.class);
    when(response.getStatus()).thenReturn(Status.SUCCESS_OK);
    when(response.getEntity()).thenReturn(new JsonRepresentation(
        (new Scanner(getClass().getResource("test-data/catalog.json").getFile())
            .next())));

    DataStore ds = testCreateDataStore(NAMESPACE, URL, null, null);
    assertEquals(ds.getTypeNames().length, 4);
    assertEquals(ds.getTypeNames()[0], "LGA Profile 2014 (beta)");
    assertEquals(ds.getTypeNames()[1], "Hospital Locations");
    assertEquals(ds.getTypeNames()[2], "SportandRec");
    assertEquals(ds.getTypeNames()[3], "ServiceAreas");
  }

}
