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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;

import org.geotools.feature.FeatureImpl;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.collection.FeatureIteratorImpl;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.identity.FeatureId;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.MalformedJsonException;
import com.vividsolutions.jts.geom.Location;

import com.vividsolutions.jts.geom.Geometry;
import org.opengis.geometry.primitive.Primitive;

import com.vividsolutions.jts.geom.GeometryFactory;

import org.geotools.geometry.jts.GeometryBuilder;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geomgraph.Position;
import com.vividsolutions.jts.geom.MultiPolygon;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * GeoJSON parsing of simple ,mbi-dimensional features using a streaming parser
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

  static public final String GEOMETRY_POINT = "Point";
  static public final String GEOMETRY_MULTIPOINT = "MultiPoint";
  static public final String GEOMETRY_LINE = "LineString";
  static public final String GEOMETRY_MULTILINE = "MultiLineString";
  static public final String GEOMETRY_POLYGON = "Polygon";
  static public final String GEOMETRY_MULTIPOLYGON = "MultiPolygon";

  protected static final String ATTRIBUTES = "attributes";
  protected static final String GEOMETRY = "geometry";

  // Read from which features are read
  protected JsonReader reader;

  /**
   * Constructor
   * 
   * @param iStream
   *          the stream to read features from
   * @throws UnsupportedEncodingException
   */
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
   * Helper funciton to convert a List of double to an array of doubles
   */
  public static double[] listToArray(List<Double> coords) {

    double[] arr = new double[coords.size()];
    int i = 0;
    for (Double d : coords) {
      arr[i++] = d.doubleValue();
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
  protected void parsePointCoordinates(List<Double> coords)
      throws JsonSyntaxException, IOException, IllegalStateException {

    this.reader.beginArray();

    // Reads the point/vertex coordinates
    while (this.reader.hasNext()) {

      // Read X and Y
      coords.add(this.reader.nextDouble());
      coords.add(this.reader.nextDouble());

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
  public double[] parseCoordinateArray()
      throws JsonSyntaxException, IOException, IllegalStateException {

    List<Double> coords = new ArrayList<Double>();

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
  public double[] parsePointCoordinates()
      throws JsonSyntaxException, IOException, IllegalStateException {

    List<Double> coords = new ArrayList<Double>();
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
  public List<double[]> parseMultiPointCoordinates()
      throws JsonSyntaxException, IOException, IllegalStateException {

    List<double[]> points = new ArrayList<double[]>();

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
  public double[] parseLineStringCoordinates()
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
  public List<double[]> parseMultiLineStringCoordinates()
      throws JsonSyntaxException, IOException, IllegalStateException {

    List<double[]> lines = new ArrayList<double[]>();

    this.reader.beginArray();
    while (this.reader.hasNext()) {
      lines.add(this.parseLineStringCoordinates());
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
  public List<double[]> parsePolygonCoordinates()
      throws JsonSyntaxException, IOException, IllegalStateException {

    List<double[]> rings = new ArrayList<double[]>();

    this.reader.beginArray();
    while (this.reader.hasNext()) {
      rings.add(this.parseLineStringCoordinates());
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
  public List<List<double[]>> parseMultiPolygonCoordinates()
      throws JsonSyntaxException, IOException, IllegalStateException {

    List<List<double[]>> polys = new ArrayList<List<double[]>>();

    this.reader.beginArray();
    while (this.reader.hasNext()) {
      polys.add(this.parsePolygonCoordinates());
    }
    this.reader.endArray();
    return polys;
  }

  /**
   * Parses a Geometry in GeoJSON format
   * 
   * @return list of arrays with ring coordinates
   * @throws IOException,
   *           JsonSyntaxException, IllegalStateException
   */
  public Geometry parseGeometry()
      throws JsonSyntaxException, IOException, IllegalStateException {

    double[] coords;
    GeometryBuilder builder = new GeometryBuilder();
    GeometryFactory geomFactory = new GeometryFactory();

    // If geometry is null, returns a null point
    try {
      if (this.reader.peek() == JsonToken.NULL) {
        this.reader.nextNull();
        throw(new MalformedJsonException("just here to avoid repeating the return statement"));
      }
    } catch (IllegalStateException | MalformedJsonException e) {
      return builder.point();
    }

    this.reader.beginObject();

    // Check the presence of feature type
    if (!reader.nextName().equals(FEATURE_TYPE)) {
      throw (new JsonSyntaxException("Geometry type expected"));
    }

    switch (reader.nextString()) {

    case GEOMETRY_POINT:
      this.checkPropertyName(FEATURE_GEOMETRY_COORDINATES);
      coords = this.parsePointCoordinates();
      this.reader.endObject();
      return (Geometry) builder.point(coords[0], coords[1]);

    case GEOMETRY_MULTIPOINT:
      this.checkPropertyName(FEATURE_GEOMETRY_COORDINATES);
      List<double[]> pointCoords = this.parseMultiPointCoordinates();
      ;
      Point[] points = new Point[pointCoords.size()];
      for (int i = 0; i < pointCoords.size(); i++) {
        points[i] = (Point) builder.point(pointCoords.get(i)[0],
            pointCoords.get(i)[1]);
      }
      this.reader.endObject();
      return (Geometry) new MultiPoint(points, geomFactory);

    case GEOMETRY_LINE:
      this.checkPropertyName(FEATURE_GEOMETRY_COORDINATES);
      coords = this.parseLineStringCoordinates();
      this.reader.endObject();
      return (Geometry) builder.lineString(coords);

    case GEOMETRY_POLYGON:
      this.checkPropertyName(FEATURE_GEOMETRY_COORDINATES);
      List<double[]> rings = this.parsePolygonCoordinates();
      this.reader.endObject();
      return (Geometry) builder.polygon(rings.get(0)); // FIXME: what about
                                                       // holes?

    case GEOMETRY_MULTIPOLYGON:
      this.checkPropertyName(FEATURE_GEOMETRY_COORDINATES);
      List<List<double[]>> polyArrays = this.parseMultiPolygonCoordinates();
      Polygon[] polys = new Polygon[polyArrays.size()];
      int i = 0;
      for (List<double[]> array : polyArrays) {
        polys[i++] = builder.polygon(array.get(0)); // FIXME: what about holes?
      }
      this.reader.endObject();
      return (Geometry) builder.multiPolygon(polys);

    default:
      throw (new JsonSyntaxException("Unrecognized geometry type"));
    }

  }

  /**
   * Parses a GeoJSON feature properties. The values returned in a map is a
   * Boolean, a String, or a Double (for every numeric values)
   * 
   * @return A map with property names as keys, and property values as values
   * 
   * @throws IOException,
   *           JsonSyntaxException, IllegalStateException
   */
  public Map<String, Object> parseProperties()
      throws JsonSyntaxException, IOException, IllegalStateException {

    Map<String, Object> props = new HashMap<String, Object>();
    String name;

    // If properties is null, returns a null point
    // If geometry is null, returns a null point
    try {
      if (this.reader.peek() == JsonToken.NULL) {
        this.reader.nextNull();
        throw(new MalformedJsonException("just here to avoid repeating the return statement"));
      }
    } catch (IllegalStateException | MalformedJsonException e) {
      return props;
    }

    this.reader.beginObject();

    try {
      while (this.reader.hasNext()) {
        name = this.reader.nextName();

        switch (this.reader.peek()) {

        case BOOLEAN:
          props.put(name, this.reader.nextBoolean());
          break;

        case NUMBER:
          props.put(name, this.reader.nextDouble());
          break;

        case STRING:
          props.put(name, this.reader.nextString());
          break;

        default:
          throw (new JsonSyntaxException("Value expected"));
        }
      }
    } catch (IOException | IllegalStateException e) {
      throw (new NoSuchElementException(e.getMessage()));
    }

    this.reader.endObject();

    return props;
  }

  /**
   * Parses a GeoJSON feature that conforms to the given FeatureType
   * 
   * @param featureType
   *          The type the feature has to conform to
   * 
   * @return the parsed feature
   */
  public SimpleFeature parseFeature(SimpleFeatureType featureType) {

    Geometry geom = null;
    Map<String, Object> props = new HashMap<String, Object>();
    List<Object> values = new ArrayList();

    // Parses the feature
    try {
      this.reader.beginObject();

      while (this.reader.hasNext()) {

        String s = this.reader.nextName(); // XXX
        switch (s) {

        case ArcGISRestFeatureReader.FEATURE_TYPE:
          if (!FEATURE_TYPE_VALUE.equals(this.reader.nextString())) {
            throw (new JsonSyntaxException(
                "Type should be equal to '" + FEATURE_TYPE_VALUE + "'"));
          }
          break;

        case ArcGISRestFeatureReader.FEATURE_GEOMETRY:
          geom = this.parseGeometry();
          break;

        case ArcGISRestFeatureReader.FEATURE_PROPERTIES:
          props = this.parseProperties();
          break;

        default:
          throw (new JsonSyntaxException("Unrecognized feature format"));
        }
      }

      this.reader.endObject();

    } catch (IOException | IllegalStateException e) {
      throw (new NoSuchElementException(e.getMessage()));
    }

    // Builds the feature, inserting the properties in an array in the same
    // order of the atterbiutes in the feature type
    
    for (AttributeDescriptor attr : featureType.getAttributeDescriptors()) {

//      if (featureType.getGeometryDescriptor().getLocalName().equals(attr.getLocalName())) {
//        values.add(geom);
//      }

      if (props.get(attr.getLocalName()) != null) {
        values.add(props.get(attr.getLocalName()));
      }
    }

    SimpleFeature feat= new SimpleFeatureImpl(values, featureType,
        SimpleFeatureBuilder.createDefaultFeatureIdentifier(FEATURES)); // TODO:
    feat.setDefaultGeometry(geom);
    
    return feat;
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

  /**
   * Checks the next token is expProp, trow an exception if not
   * 
   * @param expProp
   *          expected property name
   * @throws JsonSyntaxException
   *           ,IoException
   */
  protected void checkPropertyName(String expProp)
      throws JsonSyntaxException, IOException {

    if (!expProp.equals(this.reader.nextName())) {
      throw (new JsonSyntaxException("'" + expProp + "' property expected"));
    }
  }
}
