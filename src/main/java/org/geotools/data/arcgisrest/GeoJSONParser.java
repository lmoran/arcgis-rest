package org.geotools.data.arcgisrest;

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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;

import org.geotools.feature.FeatureIterator;
import org.geotools.feature.collection.FeatureIteratorImpl;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * GeoJSON parsing of simple features using a streaming parser
 * 
 * @author lmorandini
 *
 */
public class GeoJSONParser {

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

  protected JsonReader reader;

  public GeoJSONParser(InputStream iStream)
      throws UnsupportedEncodingException {
    this.reader = new JsonReader(new InputStreamReader(iStream, "UTF-8")); // FIXME:
  }

  // TODO
  public FeatureIterator<SimpleFeature> parseFeatureCollection(
      SimpleFeatureType featureTypeIn) {

    /*
     * this.featureType = featureTypeIn; this.featIndex = 0; this.LOGGER =
     * logger;
     * 
     * // Processes what is not the features array, and stops when the array is
     * // found this.reader.beginObject(); while (this.reader.hasNext()) {
     * String name = this.reader.nextName(); System.out.println("**** 0 " +
     * name); if (name.equals(ArcGISRestFeatureReader.FEATURES)) {
     * this.reader.beginArray(); return; } else { this.reader.skipValue(); } }
     */
    return null;
  }

  /**
   * Helper funciton to convert a List of Float to an array of floats
   */
  public static float[] listToArray(List<Float> coords) {

    float[] arr = new float[coords.size()];
    int i = 0;
    for (Float f : coords) {
      arr[i++] = f.floatValue();
    }
    return arr;
  }

  /**
   * Utility methof that parses a Point GeoJSON coordinates array and adds them
   * to coords
   * 
   * @param coords
   *          List to add coordinates to
   * @throws IOException,
   *           JsonSyntaxException, IllegalStateException
   */
  protected void parsePointCoordinates(List<Float> coords)
      throws JsonSyntaxException, IOException, IllegalStateException {

    this.reader.beginArray();

    // Reads the point/vertex coordinates
    while (this.reader.hasNext()) {

      // Read X and Y
      coords.add((float) this.reader.nextDouble());
      coords.add((float) this.reader.nextDouble());

      // FIXME: Discards Z
      if (this.reader.peek() == JsonToken.NUMBER) {
        this.reader.skipValue();
      }
    }

    this.reader.endArray();
  }

  /**
   * Parses a GeoJSON coordinates array (it is an Array of point coordinates
   * expressed as Array) and returns it a simple double arrays
   * 
   * @return array with coordinates
   * @throws IOException,
   *           JsonSyntaxException, IllegalStateException
   */
  public float[] parseCoordinateArray()
      throws JsonSyntaxException, IOException, IllegalStateException {

    List<Float> coords = new ArrayList<Float>();

    this.reader.beginArray();

    while (this.reader.hasNext()) {
      this.parsePointCoordinates(coords);
    }

    this.reader.endArray();

    return GeoJSONParser.listToArray(coords);
  }

  /**
   * Parses a Point GeoJSON coordinates array and returns them in an array
   * 
   * @return array with coordinates
   * @throws IOException,
   *           JsonSyntaxException, IllegalStateException
   */
  public float[] parsePointCoordinates()
      throws JsonSyntaxException, IOException, IllegalStateException {

    List<Float> coords = new ArrayList<Float>();
    this.parsePointCoordinates(coords);
    return GeoJSONParser.listToArray(coords);
  }

  /**
   * Parses a MultiPoint GeoJSON coordinates array and adds them to coords
   * 
   * @return list of arrays with coordinates
   * @throws IOException,
   *           JsonSyntaxException, IllegalStateException
   */
  public List<float[]> parseMultiPointCoordinates()
      throws JsonSyntaxException, IOException, IllegalStateException {

    List<float[]> points = new ArrayList<float[]>();
    
    this.reader.beginArray();
    while (this.reader.hasNext()) {
      points.add(this.parsePointCoordinates());
    }
    this.reader.endArray();

    return points;
  }

  /**
   * Parses a Line GeoJSON coordinates array and adds them to coords
   * 
   * @return array with coordinates
   * @throws IOException,
   *           JsonSyntaxException, IllegalStateExceptionadds them to coords
   */
  public float[] parseLineCoordinates()
      throws JsonSyntaxException, IOException, IllegalStateException {

    return this.parseCoordinateArray();
  }

  /**
   * Parses a MultiLine GeoJSON coordinates array and adds them to coords
   * 
   * @return list of arrays with coordinates
   * @throws IOException,
   *           JsonSyntaxException, IllegalStateException
   */
  public List<float[]> parseMultiLineCoordinates()
      throws JsonSyntaxException, IOException, IllegalStateException {

    List<float[]> lines = new ArrayList<float[]>();

    this.reader.beginArray();
    while (this.reader.hasNext()) {
      lines.add(this.parseLineCoordinates());
    }
    this.reader.endArray();
    return lines;
  }

  /**
   * Parses a Polygon GeoJSON coordinates array and adds them to coords
   * 
   * @return list of arrays with coordinates
   * @throws IOException,
   *           JsonSyntaxException, IllegalStateException
   */
  public List<float[]> parsePolygonCoordinates()
      throws JsonSyntaxException, IOException, IllegalStateException {

    List<float[]> rings = new ArrayList<float[]>();

    this.reader.beginArray();
    while (this.reader.hasNext()) {
      rings.add(this.parseLineCoordinates());
    }
    this.reader.endArray();
    return rings;
  }

  /**
   * Parses a MultiPolygon GeoJSON coordinates array and adds them to coords
   * 
   * @return list of arrays with ring coordinates
   * @throws IOException,
   *           JsonSyntaxException, IllegalStateException
   */
  public List<List<float[]>> parseMultiPolygonCoordinates()
      throws JsonSyntaxException, IOException, IllegalStateException {

    List<List<float[]>> polys = new ArrayList<List<float[]>>();

    this.reader.beginArray();
    while (this.reader.hasNext()) {
      polys.add(this.parsePolygonCoordinates());
    }
    this.reader.endArray();
    return polys;
  }

  /**
   * Parses a GepJSON feature that conforms to the given FeatureType
   * 
   * @param featureType
   *          The type the feature has to conform to
   * 
   * @return the parsed feature TODO
   */
  public SimpleFeature parseFeature(SimpleFeatureType featureType) {
    try {
      this.reader.beginObject();

      while (this.reader.hasNext()) {
        String name = this.reader.nextName();
        System.out.println("**** inside feature " + name);

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
            System.out.println("**** geomName " + geomName);

            String geomType;
            switch (geomName) {

            case ArcGISRestFeatureReader.FEATURE_GEOMETRY_TYPE:
              geomType = reader.nextString();
              System.out.println("**** geomTyoe " + geomType);
              break;

            case ArcGISRestFeatureReader.FEATURE_GEOMETRY_COORDINATES:
              System.out.println("**** coordinates ");
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
            System.out.println("**** prop " + name);
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

    JsonToken token = reader.peek();
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
    return new FeatureIteratorImpl<SimpleFeature>(null);
  }
}
