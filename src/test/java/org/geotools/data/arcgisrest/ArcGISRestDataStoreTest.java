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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HostParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.store.ContentEntry;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.NameImpl;
import org.geotools.referencing.CRS;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import java.io.IOException;
import java.net.URL;

import org.powermock.modules.junit4.PowerMockRunner;

import com.vividsolutions.jts.geom.Geometry;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ HttpMethod.class, ArcGISRestDataStore.class })
public class ArcGISRestDataStoreTest {

  protected ArcGISRestDataStore dataStore;

  public static String TYPENAME1 = "LGA Profile 2014 (beta)";
  public static String TYPENAME2 = "Hospital Locations";
  public static String TYPENAME3 = "SportandRec";
  public static String TYPENAME4 = "ServiceAreas";

  public void setCatalogMock() throws Exception {
    // Catalog mock
    HttpClient catalogClientMock = PowerMockito.mock(HttpClient.class);
    PowerMockito.whenNew(HttpClient.class).withNoArguments()
        .thenReturn(catalogClientMock);

    GetMethod catalogMock = PowerMockito.mock(GetMethod.class);
    PowerMockito.whenNew(GetMethod.class)
        .withArguments(ArcGISRestDataStoreFactoryTest.URL)
        .thenReturn(catalogMock);

    when(catalogClientMock.executeMethod(catalogMock))
        .thenReturn(HttpStatus.SC_OK);

    when(catalogMock.getResponseBodyAsString()).thenReturn(
        ArcGISRestDataStoreFactoryTest.readJSON("test-data/catalog.json"));

  }

  public void setLayerMock() throws Exception {
    // WebService mock
    HttpClient wsClientMock = PowerMockito.mock(HttpClient.class);
    PowerMockito.whenNew(HttpClient.class).withNoArguments()
        .thenReturn(wsClientMock);

    GetMethod wsMock = PowerMockito.mock(GetMethod.class);
    PowerMockito.whenNew(GetMethod.class)
        .withArguments(ArcGISRestDataStoreFactoryTest.WSURL).thenReturn(wsMock);

    when(wsClientMock.executeMethod(wsMock)).thenReturn(HttpStatus.SC_OK);
    when(wsMock.getResponseBodyAsString()).thenReturn(
        ArcGISRestDataStoreFactoryTest.readJSON("test-data/lgaDataset.json"));
  }

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testHTTPError() throws Exception {

    // Catalog mock
    HttpClient catalogClientMock = PowerMockito.mock(HttpClient.class);
    PowerMockito.whenNew(HttpClient.class).withNoArguments()
        .thenReturn(catalogClientMock);

    GetMethod catalogMock = PowerMockito.mock(GetMethod.class);
    PowerMockito.whenNew(GetMethod.class)
        .withArguments(ArcGISRestDataStoreFactoryTest.URL)
        .thenReturn(catalogMock);

    when(catalogClientMock.executeMethod(catalogMock))
        .thenReturn(HttpStatus.SC_NOT_FOUND);

    when(catalogMock.getResponseBodyAsString()).thenReturn(null);

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
    HttpClient catalogClientMock = PowerMockito.mock(HttpClient.class);
    PowerMockito.whenNew(HttpClient.class).withNoArguments()
        .thenReturn(catalogClientMock);

    GetMethod catalogMock = PowerMockito.mock(GetMethod.class);
    PowerMockito.whenNew(GetMethod.class)
        .withArguments(ArcGISRestDataStoreFactoryTest.URL)
        .thenReturn(catalogMock);

    when(catalogClientMock.executeMethod(catalogMock))
        .thenReturn(HttpStatus.SC_OK);

    when(catalogMock.getResponseBodyAsString()).thenReturn(
        ArcGISRestDataStoreFactoryTest.readJSON("test-data/error.json"));

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
    HttpClient catalogClientMock = PowerMockito.mock(HttpClient.class);
    PowerMockito.whenNew(HttpClient.class).withNoArguments()
        .thenReturn(catalogClientMock);

    GetMethod catalogMock = PowerMockito.mock(GetMethod.class);
    PowerMockito.whenNew(GetMethod.class)
        .withArguments(ArcGISRestDataStoreFactoryTest.URL)
        .thenReturn(catalogMock);

    when(catalogClientMock.executeMethod(catalogMock))
        .thenReturn(HttpStatus.SC_OK);

    when(catalogMock.getResponseBodyAsString()).thenReturn(
        ArcGISRestDataStoreFactoryTest.readJSON("test-data/catalog.json"));

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

    assertNotNull(this.dataStore.getEntry(
        new NameImpl(ArcGISRestDataStoreFactoryTest.NAMESPACE, TYPENAME1)));
  }

  @Test
  public void testCreateFeatureSource() throws Exception {

    this.setCatalogMock();

    this.dataStore = (ArcGISRestDataStore) ArcGISRestDataStoreFactoryTest
        .createDefaultTestDataStore();
    this.dataStore.createTypeNames();

    this.setLayerMock();

    FeatureSource<SimpleFeatureType, SimpleFeature> src = this.dataStore
        .createFeatureSource(this.dataStore.getEntry(
            new NameImpl(ArcGISRestDataStoreFactoryTest.NAMESPACE, TYPENAME1)));
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

    // Feature count mock
    HttpClient countClientMock = PowerMockito.mock(HttpClient.class);
    PowerMockito.whenNew(HttpClient.class).withNoArguments()
        .thenReturn(countClientMock);

    GetMethod countMock = PowerMockito.mock(GetMethod.class);
    PowerMockito.whenNew(GetMethod.class)
        .withArguments(ArcGISRestDataStoreFactoryTest.QUERYURL)
        .thenReturn(countMock);

    when(countClientMock.executeMethod(countMock)).thenReturn(HttpStatus.SC_OK);
    when(countMock.getResponseBodyAsString()).thenReturn(
        ArcGISRestDataStoreFactoryTest.readJSON("test-data/count.json"));
  }

  @Test
  public void testCount() throws Exception {

    this.setCatalogMock();

    this.dataStore = (ArcGISRestDataStore) ArcGISRestDataStoreFactoryTest
        .createDefaultTestDataStore();
    this.dataStore.createTypeNames();

    this.setLayerMock();

    FeatureSource<SimpleFeatureType, SimpleFeature> src = this.dataStore
        .createFeatureSource(this.dataStore.getEntry(
            new NameImpl(ArcGISRestDataStoreFactoryTest.NAMESPACE, TYPENAME1)));

    // Feature count mock
    HttpClient countClientMock = PowerMockito.mock(HttpClient.class);
    PowerMockito.whenNew(HttpClient.class).withNoArguments()
        .thenReturn(countClientMock);

    GetMethod countMock = PowerMockito.mock(GetMethod.class);
    PowerMockito.whenNew(GetMethod.class)
        .withArguments(ArcGISRestDataStoreFactoryTest.QUERYURL)
        .thenReturn(countMock);

    when(countClientMock.executeMethod(countMock)).thenReturn(HttpStatus.SC_OK);
    when(countMock.getResponseBodyAsString()).thenReturn(
        ArcGISRestDataStoreFactoryTest.readJSON("test-data/count.json"));

    assertEquals(79, src.getCount(new Query()));
  }

  @Test
  public void testFeatures() throws Exception {

    this.setCatalogMock();

    this.dataStore = (ArcGISRestDataStore) ArcGISRestDataStoreFactoryTest
        .createDefaultTestDataStore();
    this.dataStore.createTypeNames();

    this.setLayerMock();

    FeatureSource<SimpleFeatureType, SimpleFeature> src = this.dataStore
        .createFeatureSource(this.dataStore.getEntry(
            new NameImpl(ArcGISRestDataStoreFactoryTest.NAMESPACE, TYPENAME1)));

    // Features mock
    HttpClient featClientMock = PowerMockito.mock(HttpClient.class);
    PowerMockito.whenNew(HttpClient.class).withNoArguments()
        .thenReturn(featClientMock);

    GetMethod featMock = PowerMockito.mock(GetMethod.class);
    PowerMockito.whenNew(GetMethod.class)
        .withArguments(ArcGISRestDataStoreFactoryTest.QUERYURL)
        .thenReturn(featMock);

    when(featClientMock.executeMethod(featMock)).thenReturn(HttpStatus.SC_OK);
    when(featMock.getResponseBodyAsString()).thenReturn(
        ArcGISRestDataStoreFactoryTest.readJSON("test-data/lgaFeatures.json"));

    FeatureIterator iter = src.getFeatures(new Query()).features();
    SimpleFeature sf;
    assertEquals(true, iter.hasNext());
    sf= (SimpleFeature) iter.next();
    assertEquals(true, iter.hasNext());
    sf= (SimpleFeature) iter.next();
    assertEquals(true, iter.hasNext());
    sf= (SimpleFeature) iter.next();
    assertEquals("POINT (15727181.152716042 -4394485.520718031)", ((Geometry)(sf.getAttribute("the_geom"))).getCentroid().toString());
    assertEquals("West Wimmera (S)", sf.getAttribute("LGA"));
    assertEquals(false, iter.hasNext());
    assertEquals(false, iter.hasNext());
  }
}
