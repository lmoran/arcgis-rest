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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.geotools.data.DefaultResourceInfo;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.ResourceInfo;
import org.geotools.data.arcgisrest.schema.catalog.Dataset;
import org.geotools.data.arcgisrest.schema.webservice.Webservice;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.feature.NameImpl;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
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

  protected static Map<String, Class> EsriJavaMapping = new HashMap<String, Class>();
  static {
    EsriJavaMapping.put("esriFieldTypeBlob", java.lang.Object.class);
    EsriJavaMapping.put("esriFieldTypeDate", java.util.Date.class);
    EsriJavaMapping.put("esriFieldTypeDouble", java.lang.Double.class);
    EsriJavaMapping.put("esriFieldTypeGUID", java.lang.String.class);
    // TODO: EsriJavaMapping.put("esriFieldTypeGeometry", "");
    EsriJavaMapping.put("esriFieldTypeGlobalID", java.lang.Long.class);
    EsriJavaMapping.put("esriFieldTypeInteger", java.lang.Integer.class);
    EsriJavaMapping.put("esriFieldTypeOID", java.lang.String.class);
    EsriJavaMapping.put("esriFieldTypeRaster", java.lang.Object.class);
    EsriJavaMapping.put("esriFieldTypeSingle", java.lang.Float.class);
    EsriJavaMapping.put("esriFieldTypeSmallInteger", java.lang.Integer.class);
    EsriJavaMapping.put("esriFieldTypeString", java.lang.String.class);
    EsriJavaMapping.put("esriFieldTypeXML", java.lang.String.class);
  }

  public ArcGISRestFeatureSource(ContentEntry entry, Query query)
      throws IOException, URISyntaxException, NoSuchAuthorityCodeException,
      FactoryException {

    super(entry, query);

    this.dataStore = (ArcGISRestDataStore) entry.getDataStore();

    // Puts in typeName the typename to create a feature source
    // from, and throws an exeption if entry is not in the catalog datasets
    int typeIndex = this.dataStore.createTypeNames().indexOf(entry.getName());
    if (typeIndex == -1) {
      throw new IOException("Type name " + entry.getName() + " not found");
    }
    Dataset typeName = this.dataStore.getCatalog().getDataset().get(typeIndex);

    // Retrieves the dataset JSON document
    URL dsUrl = new URL(typeName.getWebService().toString());
    this.ws = (new Gson()).fromJson(this.dataStore.retrieveJSON(dsUrl),
        Webservice.class);

    // Sets the resource info
    this.resInfo = new DefaultResourceInfo();
    this.resInfo
        .setSchema(new URI(this.dataStore.getNamespace().toExternalForm()));
    this.resInfo.setCRS(CRS.decode(
        "EPSG:" + this.ws.getExtent().getSpatialReference().getLatestWkid()));
    this.resInfo.setDescription(this.ws.getDescription().length() > 2? this.ws.getDescription() : typeName.getDescription());
    this.resInfo.setKeywords(new HashSet(typeName.getKeyword()));

    this.resInfo.setTitle(typeName.getTitle());
    this.resInfo.setName(this.ws.getName());
    ReferencedEnvelope geoBbox = new ReferencedEnvelope(
        this.ws.getExtent().getXmin(), this.ws.getExtent().getXmax(),
        this.ws.getExtent().getYmin(), this.ws.getExtent().getYmax(),
        this.resInfo.getCRS());
    this.resInfo.setBounds(geoBbox);
  }

  @Override
  protected SimpleFeatureType buildFeatureType() throws IOException {

    SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
    builder.setName(new NameImpl(this.resInfo.getSchema().toString(),
        this.resInfo.getName()));

    this.ws.getFields().forEach((fld) -> {
      Class clazz = EsriJavaMapping.get(fld.getType());
      if (clazz == null) {
        this.getDataStore().getLogger()
            .severe("Type " + fld.getType() + " not found");
      }
      builder.add(fld.getName(), clazz);
    });

    return builder.buildFeatureType();
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
