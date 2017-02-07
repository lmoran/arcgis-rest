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
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.FeatureReader;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.collection.FeatureIteratorImpl;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.geotools.geojson.feature.FeatureJSON;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class ArcGISRestFeatureReader
    implements FeatureReader<SimpleFeatureType, SimpleFeature> {

  /**
   * GeoJSON format constants
   */
  static public final String GEOJSON_TYPE = "type";
  static public final String GEOJSON_TYPE_VALUE_FC = "FeatureCollection";
  static public final String FEATURES = "features";
  static public final String FEATURE_TYPE = "type";
  static public final String FEATURE_GEOMETRY = "geometry";
  static public final String FEATURE_GEOMETRY_TYPE = "type";
  static public final String FEATURE_TYPE_VALUE = "Feature";
  static public final String FEATURE_GEOMETRY_COORDINATES = "coordinates";
  static public final String FEATURE_PROPERTIES = "properties";

  protected static final String ATTRIBUTES = "attributes";
  protected static final String GEOMETRY = "geometry";

  protected FeatureIterator<SimpleFeature> features;
  protected SimpleFeatureType featureType;
  protected JsonReader reader;
  protected Logger LOGGER;

  protected int featIndex = 0;

  public ArcGISRestFeatureReader(SimpleFeatureType featureTypeIn,
      InputStream iStream, Logger logger) throws IOException {
    this.reader = new JsonReader(new InputStreamReader(iStream, "UTF-8")); // FIXME:
    // this.features = (new FeatureJSON()).streamFeatureCollection(resultIn);
    this.featureType = featureTypeIn;
    this.featIndex = 0;
    this.LOGGER = logger;

    // Processes what is not the features array, and stops when the array is
    // found
    this.reader.beginObject();
    while (this.reader.hasNext()) {
      String name = this.reader.nextName();
      this.LOGGER.log(Level.SEVERE, "**** 0 " + name);
      if (name.equals(ArcGISRestFeatureReader.FEATURES)) {
        this.reader.beginArray();
        return;
      } else {
        this.reader.skipValue();
      }
    }
    // this.reader.endObject();
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
  public boolean hasNext() {

    JsonToken token = null;
    try {
      token = this.reader.peek();
      if (token == com.google.gson.stream.JsonToken.BEGIN_OBJECT) {
        return true;
      }
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }

    return false;
  }

  /**
   * @throws IOException
   * @see FeatureReader#next()
   */
  @Override
  public SimpleFeature next() throws NoSuchElementException, IOException {
    // https://github.com/geotools/geotools/blob/master/modules/library/data/src/main/java/org/geotools/data/store/ContentDataStore.java

    String geomType;

    /**
     * Reads a single feature
     */
    try {
      this.reader.beginObject();

      while (this.reader.hasNext()) {
        String name = this.reader.nextName();
        this.LOGGER.log(Level.SEVERE, "**** inside feature " + name);

        switch (name) {

        case ArcGISRestFeatureReader.FEATURE_TYPE:

          if (ArcGISRestFeatureReader.FEATURE_TYPE
              .equals(reader.nextString())) {
            throw new IOException("Incorrect feature type ");
          }
          break;

        case ArcGISRestFeatureReader.FEATURE_GEOMETRY:

          this.reader.beginObject();
          while (this.reader.hasNext()) {
            String geomName = this.reader.nextName();
            this.LOGGER.log(Level.SEVERE, "**** geomName " + geomName);

            switch (geomName) {

            case ArcGISRestFeatureReader.FEATURE_GEOMETRY_TYPE:
              geomType = reader.nextString();
              this.LOGGER.log(Level.SEVERE, "**** geomTyoe " + geomType);
              break;

            case ArcGISRestFeatureReader.FEATURE_GEOMETRY_COORDINATES:
              this.LOGGER.log(Level.SEVERE, "**** coordinates ");
              reader.beginArray();
              while (reader.hasNext()) {
                reader.skipValue(); // XXX
              }
              reader.endArray();
              break;
            }
          }
          this.reader.endObject();

        case ArcGISRestFeatureReader.FEATURE_PROPERTIES:
          this.reader.beginObject();
          while (this.reader.hasNext()) {
            this.LOGGER.log(Level.SEVERE, "**** prop " + name);
            reader.skipValue();
          }
          this.reader.endObject();
          break;
        }

      }

      this.reader.endObject();

      // (new Gson()).fromJson(json, classOfT)
      return new SimpleFeatureImpl(null, featureType, null);

    } catch (IOException | IllegalStateException e) {
      throw (new NoSuchElementException(e.getMessage()));
    }

  }

  @Override
  public void close() {
    try {
      this.reader.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * Returns an iterator to navigate the features in the GeoJSON input stream.
   * Since ArcGIS ReST API may return an error message as a JSON (not a
   * GeoJSON), this case is handled by throwing an exception
   * 
   * @return A simple feature collection iterator
   * @throws IOException
   */
  public FeatureIterator<SimpleFeature> streamFeatureCollection()
      throws IOException {

    // Processes what not the features array
    this.reader.beginObject();
    while (this.reader.hasNext()) {
      String name = this.reader.nextName();
      if (name.equals(ArcGISRestFeatureReader.GEOJSON_TYPE_VALUE_FC)) {
        this.reader.beginArray();
      } else {
        this.reader.skipValue();
      }
    }
    this.reader.endObject();

    /**
     * switch (token) { case BEGIN_ARRAY: reader.beginArray(); //
     * writer.beginArray(); break; case END_ARRAY: reader.endArray(); //
     * writer.endArray(); break; case BEGIN_OBJECT: reader.beginObject(); //
     * writer.beginObject(); break; case END_OBJECT: reader.endObject(); //
     * writer.endObject(); break; case NAME: String name = reader.nextName(); //
     * writer.name(name); break; case STRING: String s = reader.nextString(); //
     * writer.value(s); break; case NUMBER: String n = reader.nextString(); //
     * writer.value(new BigDecimal(n)); break; case BOOLEAN: boolean b =
     * reader.nextBoolean(); // writer.value(b); break; case NULL:
     * reader.nextNull(); // writer.nullValue(); break; case END_DOCUMENT: //
     * return; }
     */
    return new FeatureIteratorImpl(null);
  }

  /**
   * Parses GepJSON a coordinate array (it is an Array of point coordinates
   * expressed as Array) and returns it a simple double arrays
   * 
   * @return double array with coordinates
   */
  protected double[] parseCoordinateArray() {
    List<Double> coords = new ArrayList<Double>();

    coords.add(1.2);
    coords.add(3.4);
    double[] coordsOut = new double[coords.size()];

    return coordsOut;
  }
}
