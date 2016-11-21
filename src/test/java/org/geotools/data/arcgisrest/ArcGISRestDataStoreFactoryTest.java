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

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.Parameter;
import org.geotools.data.DataAccessFactory.Param;
import org.geotools.data.arcgisrest.ArcGISRestDataStoreFactory;
import org.geotools.util.SimpleInternationalString;
import org.geotools.util.Version;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @source $URL$
 */
public class ArcGISRestDataStoreFactoryTest {

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

  @Test(expected=MalformedURLException.class)
  public void testCreateDataStoreMalformedNamespace() throws IOException {
    testCreateDataStore("aaa", "bbb", "ccc" ,"ddd");
  }

  @Test(expected=MalformedURLException.class)
  public void testCreateDataStoreMalformedURL() throws IOException {
    testCreateDataStore(NAMESPACE, "bbb", "ccc" ,"ddd");
  }

  /**
   * @param capabilitiesFile
   *          the name of the GetCapabilities document under
   *          {@code /org/geotools/data/wfs/impl/test-data}
   */
  private DataStore testCreateDataStore(final String namespace, final String url,
      final String user, final String password) throws IOException {

    params.put(ArcGISRestDataStoreFactory.NAMESPACE_PARAM.key, namespace);
    params.put(ArcGISRestDataStoreFactory.URL_PARAM.key, url);
    params.put(ArcGISRestDataStoreFactory.USER_PARAM.key, user);
    params.put(ArcGISRestDataStoreFactory.PASSWORD_PARAM.key, password);
/*
    final URL capabilitiesUrl = getClass()
        .getResource("test-data/" + capabilitiesFile);
    if (capabilitiesUrl == null) {
      throw new IllegalArgumentException(capabilitiesFile + " not found");
    }
    params.put(ArcGISRestDataStoreFactory.URL.key, capabilitiesUrl);
    params.put(ArcGISRestDataStoreFactory.GML_COMPLIANCE_LEVEL.key, "0");
*/
    /* FIXME: this should work with datastorefactoryfinder http://docs.geotools.org/latest/userguide/library/data/datastore.html 
    DataStore dataStore = DataStoreFinder.getDataStore(params);
    assertNotNull(dataStore);
    assertTrue(dataStore instanceof ArcGISRestDataStore);
*/
    return (new ArcGISRestDataStoreFactory()).createNewDataStore(params);
  }

  /*
  @Test
  public void testCreateNewDataStore() throws IOException {
    try {
      dsf.createNewDataStore(params);
      fail("Expected UnsupportedOperationException");
    } catch (UnsupportedOperationException e) {
      assertTrue(true);
    }
  }
    */
}
