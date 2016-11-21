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

/**
 * Generated with http://www.jsonschema2pojo.org/
 */
package org.geotools.data.arcgisrest.schema.catalog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;

public class Catalog {

  private String context;
  private String type;
  private String conformsTo;
  private String describedBy;
  private List<Dataset> dataset = new ArrayList<Dataset>();
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  /**
   * 
   * @return The context
   */
  public String getContext() {
    return context;
  }

  /**
   * 
   * @param context
   *          The @context
   */
  public void setContext(String context) {
    this.context = context;
  }

  /**
   * 
   * @return The type
   */
  public String getType() {
    return type;
  }

  /**
   * 
   * @param type
   *          The @type
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * 
   * @return The conformsTo
   */
  public String getConformsTo() {
    return conformsTo;
  }

  /**
   * 
   * @param conformsTo
   *          The conformsTo
   */
  public void setConformsTo(String conformsTo) {
    this.conformsTo = conformsTo;
  }

  /**
   * 
   * @return The describedBy
   */
  public String getDescribedBy() {
    return describedBy;
  }

  /**
   * 
   * @param describedBy
   *          The describedBy
   */
  public void setDescribedBy(String describedBy) {
    this.describedBy = describedBy;
  }

  /**
   * 
   * @return The dataset
   */
  public List<Dataset> getDataset() {
    return dataset;
  }

  /**
   * 
   * @param dataset
   *          The dataset
   */
  public void setDataset(List<Dataset> dataset) {
    this.dataset = dataset;
  }

  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }

}
