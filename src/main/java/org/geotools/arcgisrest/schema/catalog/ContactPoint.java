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

package org.geotools.arcgisrest.schema.catalog;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class ContactPoint {

  private String type;
  private String fn;
  private String hasEmail;
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

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
   * @return The fn
   */
  public String getFn() {
    return fn;
  }

  /**
   * 
   * @param fn
   *          The fn
   */
  public void setFn(String fn) {
    this.fn = fn;
  }

  /**
   * 
   * @return The hasEmail
   */
  public String getHasEmail() {
    return hasEmail;
  }

  /**
   * 
   * @param hasEmail
   *          The hasEmail
   */
  public void setHasEmail(String hasEmail) {
    this.hasEmail = hasEmail;
  }

  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }
}