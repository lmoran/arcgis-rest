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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import org.restlet.resource.ResourceException;

import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.feature.NameImpl;
import org.geotools.referencing.CRS;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import java.io.IOException;

import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ClientResource.class, ArcGISRestDataStore.class })
public class ArcGISRestDataStoreTest {

  protected ArcGISRestDataStore dataStore;

  public static String TYPENAME1 = "LGA Profile 2014 (beta)";
  public static String TYPENAME2 = "Hospital Locations";
  public static String TYPENAME3 = "SportandRec";
  public static String TYPENAME4 = "ServiceAreas";

  @Before
  public void setUp() throws Exception {

  }

  @After
  public void tearDown() throws Exception {
    // TODO
  }

  @Test
  public void testHTTPError() throws Exception {

    // Catalog mock
    ClientResource resourceCatalogMock = PowerMockito
        .mock(ClientResource.class);
    PowerMockito.whenNew(ClientResource.class)
        .withArguments(org.restlet.data.Method.GET,
            ArcGISRestDataStoreFactoryTest.URL)
        .thenReturn(resourceCatalogMock);

    when(resourceCatalogMock.get())
        .thenReturn(new StringRepresentation(null, MediaType.APPLICATION_JSON));

    when(resourceCatalogMock.getStatus())
        .thenReturn(Status.CLIENT_ERROR_NOT_FOUND);

    this.dataStore = (ArcGISRestDataStore) ArcGISRestDataStoreFactoryTest
        .createDefaultTestDataStore();
    try {
      List<Name> names = this.dataStore.createTypeNames();
    } catch (IOException e) {
      assertTrue(e.getMessage().contains("404"));
    }

  }

  @Test
  public void testServiceError() throws Exception {

    // Catalog mock
    ClientResource resourceCatalogMock = PowerMockito
        .mock(ClientResource.class);
    PowerMockito.whenNew(ClientResource.class)
        .withArguments(org.restlet.data.Method.GET,
            ArcGISRestDataStoreFactoryTest.URL)
        .thenReturn(resourceCatalogMock);

    when(resourceCatalogMock.get())
        .thenReturn(new StringRepresentation(null, MediaType.APPLICATION_JSON));

    when(resourceCatalogMock.getStatus()).thenReturn(Status.SUCCESS_OK);

    when(resourceCatalogMock.getResponseEntity())
        .thenReturn(new JsonRepresentation(
            ArcGISRestDataStoreFactoryTest.readJSON("test-data/error.json")));

    this.dataStore = (ArcGISRestDataStore) ArcGISRestDataStoreFactoryTest
        .createDefaultTestDataStore();

    try {
      List<Name> names = this.dataStore.createTypeNames();
    } catch (IOException e) {
      assertTrue(e.getMessage().contains("400 Cannot perform query"));
    }

  }

  @Test
  public void testCreateTypeNames() throws Exception {

    // Catalog mock
    ClientResource resourceCatalogMock = PowerMockito
        .mock(ClientResource.class);
    PowerMockito.whenNew(ClientResource.class)
        .withArguments(org.restlet.data.Method.GET,
            ArcGISRestDataStoreFactoryTest.URL)
        .thenReturn(resourceCatalogMock);

    when(resourceCatalogMock.get())
        .thenReturn(new StringRepresentation(null, MediaType.APPLICATION_JSON));

    when(resourceCatalogMock.getStatus()).thenReturn(Status.SUCCESS_OK);

    when(resourceCatalogMock.getResponseEntity())
        .thenReturn(new JsonRepresentation(
            ArcGISRestDataStoreFactoryTest.readJSON("test-data/catalog.json")));

    this.dataStore = (ArcGISRestDataStore) ArcGISRestDataStoreFactoryTest
        .createDefaultTestDataStore();

    List<Name> names = this.dataStore.createTypeNames();
    assertEquals(4, names.size());
    assertEquals(TYPENAME1, names.get(0).getLocalPart());
    assertEquals(TYPENAME2, names.get(1).getLocalPart());
    assertEquals(TYPENAME3, names.get(2).getLocalPart());
    assertEquals(TYPENAME4, names.get(3).getLocalPart());
    assertEquals(ArcGISRestDataStoreFactoryTest.NAMESPACE,
        names.get(0).getNamespaceURI());
  }

  @Test
  public void testGetFeatureSource() throws Exception {

    // Catalog mock
    ClientResource resourceCatalogMock = PowerMockito
        .mock(ClientResource.class);
    PowerMockito.whenNew(ClientResource.class)
        .withArguments(org.restlet.data.Method.GET,
            ArcGISRestDataStoreFactoryTest.URL)
        .thenReturn(resourceCatalogMock);

    when(resourceCatalogMock.get())
        .thenReturn(new StringRepresentation(null, MediaType.APPLICATION_JSON))
        .thenReturn(new StringRepresentation(null, MediaType.APPLICATION_JSON))
        .thenReturn(new StringRepresentation(null, MediaType.APPLICATION_JSON));

    when(resourceCatalogMock.getStatus()).thenReturn(Status.SUCCESS_OK)
        .thenReturn(Status.SUCCESS_OK).thenReturn(Status.SUCCESS_OK);

    when(resourceCatalogMock.getResponseEntity())
        .thenReturn(new JsonRepresentation(
            ArcGISRestDataStoreFactoryTest.readJSON("test-data/catalog.json")))
        .thenReturn(new JsonRepresentation(
            ArcGISRestDataStoreFactoryTest.readJSON("test-data/catalog.json")));

    // Single layer mock
    ClientResource resourceWSMock = PowerMockito.mock(ClientResource.class);
    PowerMockito.whenNew(ClientResource.class)
        .withArguments(org.restlet.data.Method.GET,
            ArcGISRestDataStoreFactoryTest.WSURL)
        .thenReturn(resourceWSMock);
    when(resourceWSMock.get())
        .thenReturn(new StringRepresentation(null, MediaType.APPLICATION_JSON));

    when(resourceWSMock.getStatus()).thenReturn(Status.SUCCESS_OK);

    when(resourceWSMock.getResponseEntity()).thenReturn(new JsonRepresentation(
        ArcGISRestDataStoreFactoryTest.readJSON("test-data/lgaDataset.json")));

    // Feature count mock
    ClientResource resourceCountMock = PowerMockito.mock(ClientResource.class);
    PowerMockito.whenNew(ClientResource.class)
        .withArguments(org.restlet.data.Method.GET,
            ArcGISRestDataStoreFactoryTest.COUNTURL)
        .thenReturn(resourceCountMock);

    when(resourceCountMock.get())
        .thenReturn(new StringRepresentation(null, MediaType.APPLICATION_JSON));

    when(resourceCountMock.getStatus()).thenReturn(Status.SUCCESS_OK);

    when(resourceCountMock.getResponseEntity())
        .thenReturn(new JsonRepresentation(
            ArcGISRestDataStoreFactoryTest.readJSON("test-data/count.json")));

    // when(resource.setChallengeResponse(null)
    // .thenReturn(ChallengeScheme.HTTP_BASIC, "", "");

    /*
     * .thenReturn(new JsonRepresentation(
     * ArcGISRestDataStoreFactoryTest.readJSON("test-data/count.json")));
     */
    this.dataStore = (ArcGISRestDataStore) ArcGISRestDataStoreFactoryTest
        .createDefaultTestDataStore();

    FeatureSource<SimpleFeatureType, SimpleFeature> src = this.dataStore
        .getFeatureSource(
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
    assertEquals(
        "<div>2014 Local Government Area Profiles</div><div><br /></div>https://www2.health.vic.gov.au/about/reporting-planning-data/gis-and-planning-products/geographical-profiles<div>&gt; Please read the data definistions at the link above</div><div>&gt; xls and pdf documents area available at the link above</div><div>&gt; This is a beta release of the 2014 LGA profiles in this format. Field names and types may change during the beta phase. </div><div><br /></div><div>Last updated : 24 May 2016</div><div>Owning agency : Department of Health and Human Services, Victoria</div><div>Copyright statement : https://www.health.vic.gov.au/copyright</div><div>Licence name : https://www.health.vic.gov.au/data-license</div><div>Disclaimer: https://www.health.vic.gov.au/data-disclaimer</div><div>Attribution statement: https://www.health.vic.gov.au/data-attribution</div><div><br /></div><div>Off-line access : Department of Health and Human Services, GPO Box 4057, Melbourne Victoria, 3001</div><div><br /></div><div>Geographic coverage-jurisdiction : Victoria</div>",
        src.getInfo().getDescription());
    assertEquals(79, src.getCount(new Query()));
  }

  /*
   * @Test public void testQuery() throws Exception {
   * 
   * }
   */
}
