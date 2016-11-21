package org.geotools.data.arcgisrest.schema.catalog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Dataset {

  private String type;
  private String identifier;
  private String title;
  private String description;
  private List<String> keyword = new ArrayList<String>();
  private String issued;
  private String modified;
  private Publisher publisher;
  private ContactPoint contactPoint;
  private String accessLevel;
  private List<Distribution> distribution = new ArrayList<Distribution>();
  private String landingPage;
  private String webService;
  private String license;
  private String spatial;
  private List<String> theme = new ArrayList<String>();
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
   * @return The identifier
   */
  public String getIdentifier() {
    return identifier;
  }

  /**
   * 
   * @param identifier
   *          The identifier
   */
  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  /**
   * 
   * @return The title
   */
  public String getTitle() {
    return title;
  }

  /**
   * 
   * @param title
   *          The title
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * 
   * @return The description
   */
  public String getDescription() {
    return description;
  }

  /**
   * 
   * @param description
   *          The description
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * 
   * @return The keyword
   */
  public List<String> getKeyword() {
    return keyword;
  }

  /**
   * 
   * @param keyword
   *          The keyword
   */
  public void setKeyword(List<String> keyword) {
    this.keyword = keyword;
  }

  /**
   * 
   * @return The issued
   */
  public String getIssued() {
    return issued;
  }

  /**
   * 
   * @param issued
   *          The issued
   */
  public void setIssued(String issued) {
    this.issued = issued;
  }

  /**
   * 
   * @return The modified
   */
  public String getModified() {
    return modified;
  }

  /**
   * 
   * @param modified
   *          The modified
   */
  public void setModified(String modified) {
    this.modified = modified;
  }

  /**
   * 
   * @return The publisher
   */
  public Publisher getPublisher() {
    return publisher;
  }

  /**
   * 
   * @param publisher
   *          The publisher
   */
  public void setPublisher(Publisher publisher) {
    this.publisher = publisher;
  }

  /**
   * 
   * @return The contactPoint
   */
  public ContactPoint getContactPoint() {
    return contactPoint;
  }

  /**
   * 
   * @param contactPoint
   *          The contactPoint
   */
  public void setContactPoint(ContactPoint contactPoint) {
    this.contactPoint = contactPoint;
  }

  /**
   * 
   * @return The accessLevel
   */
  public String getAccessLevel() {
    return accessLevel;
  }

  /**
   * 
   * @param accessLevel
   *          The accessLevel
   */
  public void setAccessLevel(String accessLevel) {
    this.accessLevel = accessLevel;
  }

  /**
   * 
   * @return The distribution
   */
  public List<Distribution> getDistribution() {
    return distribution;
  }

  /**
   * 
   * @param distribution
   *          The distribution
   */
  public void setDistribution(List<Distribution> distribution) {
    this.distribution = distribution;
  }

  /**
   * 
   * @return The landingPage
   */
  public String getLandingPage() {
    return landingPage;
  }

  /**
   * 
   * @param landingPage
   *          The landingPage
   */
  public void setLandingPage(String landingPage) {
    this.landingPage = landingPage;
  }

  /**
   * 
   * @return The webService
   */
  public String getWebService() {
    return webService;
  }

  /**
   * 
   * @param webService
   *          The webService
   */
  public void setWebService(String webService) {
    this.webService = webService;
  }

  /**
   * 
   * @return The license
   */
  public String getLicense() {
    return license;
  }

  /**
   * 
   * @param license
   *          The license
   */
  public void setLicense(String license) {
    this.license = license;
  }

  /**
   * 
   * @return The spatial
   */
  public String getSpatial() {
    return spatial;
  }

  /**
   * 
   * @param spatial
   *          The spatial
   */
  public void setSpatial(String spatial) {
    this.spatial = spatial;
  }

  /**
   * 
   * @return The theme
   */
  public List<String> getTheme() {
    return theme;
  }

  /**
   * 
   * @param theme
   *          The theme
   */
  public void setTheme(List<String> theme) {
    this.theme = theme;
  }

  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }
}