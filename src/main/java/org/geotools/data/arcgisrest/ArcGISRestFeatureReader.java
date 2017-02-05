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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import org.geotools.data.FeatureReader;
import org.geotools.data.arcgisrest.schema.query.Layer;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.MapGeometry;
import com.esri.core.geometry.OperatorExportToGeoJson;
import com.esri.core.geometry.OperatorImportFromJson;
import com.esri.core.geometry.Polygon;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;

public class ArcGISRestFeatureReader
    implements FeatureReader<SimpleFeatureType, SimpleFeature> {

  protected static String ATTRIBUTES = "attributes";
  protected static String GEOMETRY = "geometry";

  protected FeatureIterator<SimpleFeature> features;
  protected SimpleFeatureType featureType;

  protected int featIndex = 0;

  public ArcGISRestFeatureReader(SimpleFeatureType featureTypeIn,
      InputStream resultIn) throws IOException {
    this.features = (new FeatureJSON()).streamFeatureCollection(resultIn);
    this.featureType = featureTypeIn;
    this.featIndex = 0;
  }

  /**
   * @see FeatureReader#getFeatureType()
   */
  @Override
  public SimpleFeatureType getFeatureType() {
    if (this.featureType == null) {
      throw new IllegalStateException(
          "No features were retrieved, shouldn't be calling getFeatureType()");
    }
    return this.featureType;
  }

  /**
   * @see FeatureReader#hasNext()
   */
  @Override
  public boolean hasNext() throws IOException {
    return this.features.hasNext();
  }

  /**
   * @see FeatureReader#next()
   */
  @Override
  public SimpleFeature next() throws IOException, NoSuchElementException {
    return this.features.next();
  }

  @Override
  public void close() throws IOException {
    // TODO Auto-generated method stub
  }

}
