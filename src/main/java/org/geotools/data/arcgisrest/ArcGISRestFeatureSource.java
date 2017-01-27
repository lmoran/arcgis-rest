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
import java.util.Iterator;
import java.util.Map;
import java.util.StringJoiner;

import javax.xml.ws.http.HTTPException;

import org.apache.commons.httpclient.params.HttpMethodParams;
import org.geotools.data.DefaultResourceInfo;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.ResourceInfo;
import org.geotools.data.arcgisrest.schema.catalog.Dataset;
import org.geotools.data.arcgisrest.schema.query.Layer;
import org.geotools.data.arcgisrest.schema.webservice.Count;
import org.geotools.data.arcgisrest.schema.webservice.Extent;
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
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.esri.core.geometry.Geometry;
import com.google.gson.Gson;

public class ArcGISRestFeatureSource extends ContentFeatureSource {

  protected ArcGISRestDataStore dataStore;
  protected Webservice ws;
  protected SimpleFeatureType featType;
  protected DefaultResourceInfo resInfo;
  protected Dataset typeName;

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
      throws IOException {

    super(entry, query);

    this.dataStore = (ArcGISRestDataStore) entry.getDataStore();
  }

  @Override
  protected SimpleFeatureType buildFeatureType() throws IOException {

    // Puts in typeName the typename to create a feature source
    // from, and throws an exeption if entry is not in the catalog datasets
    this.typeName = null;
    this.dataStore.getCatalog().getDataset().forEach(ds -> {
      String[] s = ds.getIdentifier().split("/");
      String s2 = s[s.length - 1];
      String s3 = s2.split("_")[0];
      if (s3.equals(entry.getName().getLocalPart())) {
        this.typeName = ds;
      }
    });

    if (this.typeName == null) {
      throw new IOException("Type name " + entry.getName() + " not found");
    }

    // Retrieves the dataset JSON document
    try {
      this.ws = (new Gson()).fromJson(this.dataStore.retrieveJSON(
          new URL(typeName.getWebService().toString()),
          ArcGISRestDataStore.DEFAULT_PARAMS), Webservice.class);
    } catch (HTTPException e) {
      throw new IOException(
          "Error " + e.getStatusCode() + " " + e.getMessage());
    }

    // Sets the resource info
    this.resInfo = new DefaultResourceInfo();
    try {
      this.resInfo
          .setSchema(new URI(this.dataStore.getNamespace().toExternalForm()));
      this.resInfo.setCRS(CRS.decode(
          "EPSG:" + this.ws.getExtent().getSpatialReference().getLatestWkid()));
    } catch (URISyntaxException | FactoryException e) {
      throw new IOException(e.getMessage());
    }

    this.resInfo.setDescription(typeName.getDescription());
    this.resInfo.setKeywords(new HashSet(typeName.getKeyword()));

    this.resInfo.setTitle(typeName.getTitle());
    this.resInfo.setName(this.ws.getServiceItemId()); // XXX
    ReferencedEnvelope geoBbox = new ReferencedEnvelope(
        this.ws.getExtent().getXmin(), this.ws.getExtent().getXmax(),
        this.ws.getExtent().getYmin(), this.ws.getExtent().getYmax(),
        this.resInfo.getCRS());
    this.resInfo.setBounds(geoBbox);

    SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
    builder.setName(new NameImpl(this.resInfo.getSchema().toString(),
        this.resInfo.getName()));
    builder.add(ArcGISRestDataStore.GEOMETRY_ATTR,
        com.vividsolutions.jts.geom.Geometry.class);
    builder.setDefaultGeometry(ArcGISRestDataStore.GEOMETRY_ATTR);

    this.ws.getFields().forEach((fld) -> {
      Class clazz = EsriJavaMapping.get(fld.getType());
      if (clazz == null) {
        this.getDataStore().getLogger()
            .severe("Type " + fld.getType() + " not found");
      }
      builder.add(fld.getName(), clazz);
    });

    this.featType = builder.buildFeatureType();

    return featType;
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
    // return new NameImpl(this.ws.getName());
    return new NameImpl(this.ws.getServiceItemId());
  }

  @Override
  protected ReferencedEnvelope getBoundsInternal(Query arg0)
      throws IOException {
    if (this.resInfo == null) {
      this.buildFeatureType();
    }
    return this.resInfo.getBounds();
  }

  @Override
  protected int getCountInternal(Query query) throws IOException {

    Count cnt;
    Map<String, Object> params = new HashMap<String, Object>(
        ArcGISRestDataStore.DEFAULT_PARAMS);
    params.put(ArcGISRestDataStore.GEOMETRY_PARAM,
        this.composeExtent(this.ws.getExtent()));

    try {
      // FIXME: the URL building is rather awkward
      cnt = (new Gson()).fromJson(this.dataStore.retrieveJSON(
          (new URL(typeName.getWebService().toString() + "/query")), params),
          Count.class);
    } catch (HTTPException e) {
      throw new IOException(
          "Error " + e.getStatusCode() + " " + e.getMessage());
    }

    return cnt == null ? -1 : cnt.getCount();
  }

  @Override
  protected FeatureReader<SimpleFeatureType, SimpleFeature> getReaderInternal(
      Query query) throws IOException {

    Map<String, Object> params = new HashMap<String, Object>(
        ArcGISRestDataStore.DEFAULT_PARAMS);
    Layer result;

    // TODO: implement the query as:
    // 1) Execute the query to return the number of features 
    // 2) Paginates the query in this.ws.getMaxRecordCount() batches 
    // 3) Streams the feature collection 
    
    // TODO: sets the SRS

    // FIXME: currently it sets _only_ the BBOX query
    params.put(ArcGISRestDataStore.GEOMETRY_PARAM,
        this.composeExtent(this.getBounds(query)));

    // Sets the atttributes to return
    params.put(ArcGISRestDataStore.ATTRIBUTES_PARAM,
        this.composeAttributes(query));

    // Executes the request
    try {
      // FIXME: the URL building is rather awkward
      // FIXME: try with GeoJSON to make it faster
      // FIXME: try streaming to make it use less memory-hungry
      // (for other requests taht's accettavle, but for the actual query)
      result = (new Gson()).fromJson(this.dataStore.retrieveJSON(
          (new URL(typeName.getWebService().toString() + "/query")), params),
          Layer.class);
    } catch (HTTPException e) {
      throw new IOException(
          "Error " + e.getStatusCode() + " " + e.getMessage());
    }

    // Returns a reader for the result
    return new ArcGISRestFeatureReader(this.featType, result);
  }

  /**
   * Helper method to return an extent as the API expects it
   * 
   * @param ext
   *          Extent (as expressed in the JSON describing the layer)
   */
  protected String composeExtent(Extent ext) {
    return (new StringJoiner(",")).add(ext.getXmin().toString())
        .add(ext.getYmin().toString()).add(ext.getXmax().toString())
        .add(ext.getYmax().toString()).toString();
  }

  /**
   * Helper method to return an extent as the API expects it
   * 
   * @param ext
   *          Extent (as expressed in the JSON describing the layer)
   */
  protected String composeExtent(ReferencedEnvelope env) {
    Extent ext = new Extent();
    ext.setXmin(env.getMinX());
    ext.setXmax(env.getMaxX());
    ext.setYmin(env.getMinY());
    ext.setYmax(env.getMaxY());
    return this.composeExtent(ext);
  }

  /**
   * Helper method to return an attribute list as the API expects it
   * 
   * @param query
   *          Query to build the attributes for
   */
  protected String composeAttributes(Query query) {

    StringJoiner joiner = new StringJoiner(",");

    if (query.retrieveAllProperties()) {
      Iterator<AttributeDescriptor> iter = this.featType
          .getAttributeDescriptors().iterator();
      while (iter.hasNext()) {
        joiner.add(iter.next().getLocalName());
      }
    } else {
      for (String attr : query.getPropertyNames()) {
        joiner.add(attr);
      }
    }

    return joiner.toString();
  }

}
