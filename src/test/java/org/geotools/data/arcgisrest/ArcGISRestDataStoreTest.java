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
import org.geotools.referencing.CRS;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

public class ArcGISRestDataStoreTest {

  protected ArcGISRestDataStore dataStore;

  public static String TYPENAME1 = "LGA Profile 2014 (beta)";
  public static String TYPENAME2 = "Hospital Locations";
  public static String TYPENAME3 = "SportandRec";
  public static String TYPENAME4 = "ServiceAreas";

  @Before
  public void setUp() throws Exception {

    Response response = mock(Response.class);
    Representation entity = mock(Representation.class);
    when(response.getStatus()).thenReturn(Status.SUCCESS_OK)
        .thenReturn(Status.SUCCESS_OK);
    when(response.getEntity())
        .thenReturn(new JsonRepresentation(
            ArcGISRestDataStoreFactoryTest.readJSON("test-data/catalog.json")))
        .thenReturn(new JsonRepresentation(ArcGISRestDataStoreFactoryTest
            .readJSON("test-data/lgaDataset.json")));

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
    assertEquals(4, names.size());
    assertEquals(TYPENAME1, names.get(0).getLocalPart());
    assertEquals(TYPENAME2, names.get(1).getLocalPart());
    assertEquals(TYPENAME3, names.get(2).getLocalPart());
    assertEquals(TYPENAME4, names.get(3).getLocalPart());
  }

  @Test
  public void testCreateTypeNamesNS() throws Exception {

    List<Name> names = this.dataStore.createTypeNames();
    assertEquals(ArcGISRestDataStoreFactoryTest.NAMESPACE,
        names.get(0).getNamespaceURI());
  }

  @Test
  public void testGetFeatureSource() throws Exception {

    FeatureSource src = this.dataStore.getFeatureSource(
        new NameImpl(ArcGISRestDataStoreFactoryTest.NAMESPACE, TYPENAME1));
    assertNotNull(src);
    assertTrue(src instanceof ArcGISRestFeatureSource);

    assertEquals("LGAProfiles2014Beta", src.getInfo().getName());
    assertEquals(ArcGISRestDataStoreFactoryTest.NAMESPACE,
        src.getInfo().getSchema().toString());
    assertEquals(CRS.decode("EPSG:3857"), src.getInfo().getCRS());
    assertEquals("LGA Profile 2014 (beta)", src.getInfo().getTitle());
    assertEquals(15661191, src.getInfo().getBounds().getMinX(), 1);
    assertEquals(-4742385, src.getInfo().getBounds().getMinY(), 1);
    assertEquals(16706777, src.getInfo().getBounds().getMaxX(), 1);
    assertEquals(-4022464, src.getInfo().getBounds().getMaxY(), 1);
    assertEquals("[Health and Human Services, LGA, LGA Profiles]",
        src.getInfo().getKeywords().toString());
    assertEquals("<div>2014 Local Government Area Profiles</div><div><br /></div>https://www2.health.vic.gov.au/about/reporting-planning-data/gis-and-planning-products/geographical-profiles<div>&gt; Please read the data definistions at the link above</div><div>&gt; xls and pdf documents area available at the link above</div><div>&gt; This is a beta release of the 2014 LGA profiles in this format. Field names and types may change during the beta phase. </div><div><br /></div><div>Last updated : 24 May 2016</div><div>Owning agency : Department of Health and Human Services, Victoria</div><div>Copyright statement : https://www.health.vic.gov.au/copyright</div><div>Licence name : https://www.health.vic.gov.au/data-license</div><div>Disclaimer: https://www.health.vic.gov.au/data-disclaimer</div><div>Attribution statement: https://www.health.vic.gov.au/data-attribution</div><div><br /></div><div>Off-line access : Department of Health and Human Services, GPO Box 4057, Melbourne Victoria, 3001</div><div><br /></div><div>Geographic coverage-jurisdiction : Victoria</div>", src.getInfo().getDescription());
  }
}
