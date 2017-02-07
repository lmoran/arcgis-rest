/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2010, Open Source Geospatial Foundation (OSGeo)
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

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.util.logging.Logging;
import org.junit.Before;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.MalformedJsonException;
import com.vividsolutions.jts.geom.Geometry;

import jdk.nashorn.internal.ir.annotations.Ignore;

public class GeoJSONParserTest {

  private static final Logger LOGGER = Logging
      .getLogger("org.geotools.data.arcgisrest");

  ArcGISRestFeatureReader reader;
  SimpleFeatureType fType;
  String json;

  @Before
  public void setUp() throws Exception {

    SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
    builder.setName("jsonfeature");
    builder.add("vint", Integer.class);
    builder.add("vfloat", Float.class);
    builder.add("vstring", String.class);
    builder.add("geometry", Geometry.class);

    this.fType = builder.buildFeatureType();
  }

  @Test
  public void parsePointCoordinateList() throws Exception {

    List<Float> coordList = new ArrayList<Float>();

    (new GeoJSONParser(new ByteArrayInputStream("[1.0, 2.0]".getBytes())))
        .parsePointCoordinates(coordList);
    float[] expCoords = { 1.0f, 2.0f };

    assertArrayEquals(expCoords, GeoJSONParser.listToArray(coordList), 0.1f);
  }

  @Test
  public void parsePointCoordinateArray() throws Exception {

    float[] coords = (new GeoJSONParser(
        new ByteArrayInputStream("[1.0, 2.0]".getBytes())))
            .parsePointCoordinates();
    float[] expCoords = { 1.0f, 2.0f };

    assertArrayEquals(expCoords, coords, 0.1f);
  }

  @Test(expected = MalformedJsonException.class)
  public void parseInvalidPointCoordinate1() throws Exception {

    List<Float> coordList = new ArrayList<Float>();

    (new GeoJSONParser(new ByteArrayInputStream("[1.0 2.0]".getBytes())))
        .parsePointCoordinates(coordList);
  }

  @Test(expected = IllegalStateException.class)
  public void parseInvalidPointCoordinate2() throws Exception {

    List<Float> coordList = new ArrayList<Float>();

    (new GeoJSONParser(
        new ByteArrayInputStream("[1.0, 2.0, 3.0, 4.0]".getBytes())))
            .parsePointCoordinates(coordList);
  }

  @Test
  public void parse3DCoordinatesArray() throws Exception {

    float[] coords = (new GeoJSONParser(new ByteArrayInputStream(
        "[[102.0, 0.0, 100.0], [103.0, 1.0, 200.0], [104.0, 0.0, 300.0], [105.0, 1.0, 400.0]]"
            .getBytes()))).parseCoordinateArray();
    float[] expCoords = { 102.0f, 0.0f, 103.0f, 1.0f, 104.0f, 0.0f, 105.0f,
        1.0f };

    assertArrayEquals(expCoords, coords, 0.1f);
  }

  @Test(expected = MalformedJsonException.class)
  public void parseIncorrectCoordinatesArray1() throws Exception {

    float[] coords = (new GeoJSONParser(new ByteArrayInputStream(
        "[[102.0, 0.0], [103.0, 1.0], [104.0, 0.0 [104.5, 0.5]], [105.0, 1.0]]"
            .getBytes()))).parseCoordinateArray();
  }

  @Test
  public void parseMultiPoint() throws Exception {

    List<float[]> coords = (new GeoJSONParser(new ByteArrayInputStream(
        "[[10, 40], [40, 30], [20, 20], [30, 10]]".getBytes())))
            .parseMultiPointCoordinates();
    float[] expCoords1 = { 10.0f, 40.0f };
    float[] expCoords2 = { 40.0f, 30.0f };
    float[] expCoords3 = { 20.0f, 20.0f };
    float[] expCoords4 = { 30.0f, 10.0f, };

    assertArrayEquals(expCoords1, coords.get(0), 0.1f);
    assertArrayEquals(expCoords2, coords.get(1), 0.1f);
    assertArrayEquals(expCoords3, coords.get(2), 0.1f);
    assertArrayEquals(expCoords4, coords.get(3), 0.1f);
  }

  @Test
  public void parseLine() throws Exception {

    float[] coords = (new GeoJSONParser(new ByteArrayInputStream(
        "[[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [105.0, 1.0]]"
            .getBytes()))).parseLineCoordinates();
    float[] expCoords = { 100.0f, 0.0f, 101.0f, 0.0f, 101.0f, 1.0f, 100.0f,
        1.0f, 105.0f, 1.0f };

    assertArrayEquals(expCoords, coords, 0.1f);
  }

  @Test
  public void parseMultiLine() throws Exception {

    List<float[]> lines = (new GeoJSONParser(new ByteArrayInputStream(
        "[[[10, 10], [20, 20], [10, 40]], [[40, 40], [30, 30], [40, 20], [30, 10]]]"
            .getBytes()))).parseMultiLineCoordinates();
    float[] expLine1 = { 10.0f, 10.0f, 20.0f, 20.0f, 10.0f, 40.0f };
    float[] expLine2 = { 40.0f, 40.0f, 30.0f, 30.0f, 40.0f, 20.0f, 30.0f,
        10.0f };

    assertArrayEquals(expLine1, lines.get(0), 0.1f);
    assertArrayEquals(expLine2, lines.get(1), 0.1f);
  }

  @Test
  public void parsePolygon() throws Exception {

    List<float[]> rings = (new GeoJSONParser(new ByteArrayInputStream(
        "[[ [100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0] ]]"
            .getBytes()))).parsePolygonCoordinates();
    float[] expCoords = { 100.0f, 0.0f, 101.0f, 0.0f, 101.0f, 1.0f, 100.0f,
        1.0f, 100.0f, 0.0f };

    assertArrayEquals(expCoords, rings.get(0), 0.1f);
  }

  @Test
  public void parsePolygonWithHoles() throws Exception {

    List<float[]> rings = (new GeoJSONParser(new ByteArrayInputStream(
        "[ [[35, 10], [45, 45], [15, 40], [10, 20], [35, 10]], [[20, 30], [35, 35], [30, 20], [20, 30]] ]"
            .getBytes()))).parsePolygonCoordinates();
    float[] expRing1 = { 35.0f, 10.0f, 45.0f, 45.0f, 15.0f, 40.0f, 10.0f, 20.0f,
        35.0f, 10.0f };
    float[] expRing2 = { 20.0f, 30.0f, 35.0f, 35.0f, 30.0f, 20.0f, 20.0f,
        30.0f };

    assertArrayEquals(expRing1, rings.get(0), 0.1f);
    assertArrayEquals(expRing2, rings.get(1), 0.1f);
  }

  @Test
  public void parseMultiPolygon() throws Exception {

    List<List<float[]>> polys = (new GeoJSONParser(new ByteArrayInputStream(
        "[[ [[30, 20], [45, 40], [10, 40], [30, 20]]], [[[15, 5], [40, 10], [10, 20], [5, 10], [15, 5]] ]]"
            .getBytes()))).parseMultiPolygonCoordinates();
    float[] expPoly1 = { 30.0f, 20.0f, 45.0f, 40.0f, 10.0f, 40.0f, 30.0f,
        20.0f };
    float[] expPoly2 = { 15.0f, 5.0f, 40.0f, 10.0f, 10.0f, 20.0f, 5.0f, 10.0f,
        15.0f, 5.0f };

    assertArrayEquals(expPoly1, polys.get(0).get(0), 0.1f);
    assertArrayEquals(expPoly2, polys.get(1).get(0), 0.1f);
  }

  @Test
  public void parseMultiPolygonWithHoles() throws Exception {

    List<List<float[]>> rings = (new GeoJSONParser(new ByteArrayInputStream(
        "[[[[40, 40], [20, 45], [45, 30], [40, 40]]],[[[20, 35], [10, 30], [10, 10], [30, 5], [45, 20], [20, 35]],[[30, 20], [20, 15], [20, 25], [30, 20]]]]"
            .getBytes()))).parseMultiPolygonCoordinates();
    float[] expRing1 = { 40.0f, 40.0f, 20.0f, 45.0f, 45.0f, 30.0f, 40.0f,
        40.0f };
    float[] expRing2 = { 20.0f, 35.0f, 10.0f, 30.0f, 10.0f, 10.0f, 30.0f, 5.0f,
        45.0f, 20.0f, 20.0f, 35.0f };
    float[] expRing3 = { 30.0f, 20.0f, 20.0f, 15.0f, 20.0f, 25.0f, 30.0f,
        20.0f };

    assertArrayEquals(expRing1, rings.get(0).get(0), 0.1f);
    assertArrayEquals(expRing2, rings.get(1).get(0), 0.1f);
    assertArrayEquals(expRing3, rings.get(1).get(1), 0.1f);
  }

}
