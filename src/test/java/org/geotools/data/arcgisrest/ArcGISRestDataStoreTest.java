/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008-2016, Open Source Geospatial Foundation (OSGeo)
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

import static org.geotools.data.DataUtilities.createType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.NameImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;

public class ArcGISRestDataStoreTest {

  protected ArcGISRestDataStore dataStore;

  public static String TYPENAME1 = "LGA Profile 2014 (beta)";
  public static String TYPENAME2 = "Hospital Locations";
  public static String TYPENAME3 = "SportandRec";
  public static String TYPENAME4 = "ServiceAreas";

  @Before
  public void setUp() throws Exception {

    this.dataStore = (ArcGISRestDataStore) ArcGISRestDataStoreFactoryTest
        .createDefaultTestDataStore();
  }

  @After
  public void tearDown() throws Exception {
    // TODO
  }

  @Test
  public void testCreateTypeNames() throws Exception {

    List<Name> names = this.dataStore.createTypeNames();
    assertEquals(names.size(), 4);
    assertEquals(names.get(0).getLocalPart(), TYPENAME1);
    assertEquals(names.get(1).getLocalPart(), TYPENAME2);
    assertEquals(names.get(2).getLocalPart(), TYPENAME3);
    assertEquals(names.get(3).getLocalPart(), TYPENAME4);
  }

  @Test
  public void testCreateTypeNamesNS() throws Exception {

    List<Name> names = this.dataStore.createTypeNames();
    assertEquals(names.get(0).getNamespaceURI(),
        ArcGISRestDataStoreFactoryTest.NAMESPACE);
  }

  @Test
  public void testGetFeatureSource() throws Exception {

    FeatureSource src = this.dataStore.getFeatureSource(
        new NameImpl(ArcGISRestDataStoreFactoryTest.NAMESPACE, TYPENAME1));
    assertNotNull(src);
    assertTrue(src instanceof ArcGISRestFeatureSource);
  }
}
