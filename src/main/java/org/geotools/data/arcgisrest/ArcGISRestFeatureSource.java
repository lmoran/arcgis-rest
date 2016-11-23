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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;

import org.geotools.data.DefaultResourceInfo;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.ResourceInfo;
import org.geotools.data.arcgisrest.schema.Dataset;
import org.geotools.data.arcgisrest.schema.Webservice;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;
import org.geotools.referencing.CRS;
import org.geotools.feature.type.AttributeDescriptorImpl;
import org.geotools.feature.AttributeBuilder;
import org.geotools.feature.NameImpl;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.google.gson.Gson;

public class ArcGISRestFeatureSource extends ContentFeatureSource {

  protected ArcGISRestDataStore dataStore;
  protected Webservice ws;
  protected SimpleFeatureType featType;
  protected DefaultResourceInfo resInfo;

  // FIXME: Are we user ArcGIS ReST API always uses this for the "spatial"
  // property?
  protected static CoordinateReferenceSystem SPATIALCRS;

  {
    SPATIALCRS = CRS.decode("EPSG:4326");
  }

  protected static enum SpatialExtent {
    WestBoundLongitude(0), EastBoundLongitude(2), SouthBoundLatitude(
        1), NorthBoundLatitude(3);

    private int index;

    SpatialExtent(int i) {
      this.index = i;
    }

    public int getIndex() {
      return this.index;
    }
  };

  public ArcGISRestFeatureSource(ContentEntry entry, Query query)
      throws IOException, URISyntaxException, NoSuchAuthorityCodeException,
      FactoryException {

    super(entry, query);

    this.dataStore = (ArcGISRestDataStore) entry.getDataStore();

    // Puts in typeIndex the index of the typename to create a feature source
    // from, and throws an exeption if entry is not in the catalog datasets
    int typeIndex = this.dataStore.createTypeNames()
        .indexOf(entry.getName());
    if (typeIndex == -1) {
      throw new IOException("Type name " + entry.getName() + " not found");
    }

    // Retrieves the dataset JSON document
    URL dsUrl = new URL(this.dataStore.getCatalog().getDataset().get(typeIndex).getLandingPage().toString());
    this.ws = (new Gson()).fromJson(this.dataStore.retrieveJSON(dsUrl),
        Webservice.class);

    // Sets up the resouurce info
    this.resInfo = new DefaultResourceInfo();
    this.resInfo
        .setSchema(new URI(this.dataStore.getNamespace().toExternalForm()));
    this.resInfo.setCRS(SPATIALCRS); // FIXME: are we sure it is always in
                                     // WGS84?
    this.resInfo.setDescription(this.ws.getDescription());
// TODO    this.resInfo.setKeywords(new HashSet(this.ws.getKeyword()));
// TODO    this.resInfo.setTitle(this.ws.)
    this.resInfo.setName(this.ws.getName());
    
    /*
    "extent": {
      "coordinates": [
        [
          140.686879300941,
          -39.144459679682
        ],
        [
          150.079533856959,
          -33.9527897665956
        ]
      ]
*/
          
System.out.println("XXX " + this.ws.getExtent()); // XXX
//    String[] tokens = this.dataset.getSpatial().split(",");
/*    
    ReferencedEnvelope geoBbox = new ReferencedEnvelope(
        this.dataset.
        Double.parseDouble(tokens[SpatialExtent.EastBoundLongitude.getIndex()]),
        Double.parseDouble(tokens[SpatialExtent.SouthBoundLatitude.getIndex()]),
        Double.parseDouble(tokens[SpatialExtent.NorthBoundLatitude.getIndex()]),
        SPATIALCRS);
    this.resInfo.setBounds(geoBbox);
    */
  }

  @Override
  protected SimpleFeatureType buildFeatureType() throws IOException {

    SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
    // TODO:
    // builder.add(new AttributeDescriptorImpl(this.featType, this.dataset., 0,
    // 0, false, builder));
    return null;
  }

  @Override
  public ResourceInfo getInfo() {
    return this.resInfo;
  }

  @Override
  public ContentDataStore getDataStore() {
    return this.dataStore;
  }

  @Override
  public Name getName() {
    // TODO Auto-generated method stub
    return new NameImpl(this.ws.getName());
  }

  @Override
  protected ReferencedEnvelope getBoundsInternal(Query arg0)
      throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected int getCountInternal(Query arg0) throws IOException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  protected FeatureReader<SimpleFeatureType, SimpleFeature> getReaderInternal(
      Query arg0) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

}
