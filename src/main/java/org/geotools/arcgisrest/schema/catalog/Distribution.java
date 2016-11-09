package org.geotools.arcgisrest.schema.catalog;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Distribution {

private String type;
private String title;
private String format;
private String mediaType;
private String accessURL;
private String downloadURL;
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

/**
* 
* @return
* The type
*/
public String getType() {
return type;
}

/**
* 
* @param type
* The @type
*/
public void setType(String type) {
this.type = type;
}

/**
* 
* @return
* The title
*/
public String getTitle() {
return title;
}

/**
* 
* @param title
* The title
*/
public void setTitle(String title) {
this.title = title;
}

/**
* 
* @return
* The format
*/
public String getFormat() {
return format;
}

/**
* 
* @param format
* The format
*/
public void setFormat(String format) {
this.format = format;
}

/**
* 
* @return
* The mediaType
*/
public String getMediaType() {
return mediaType;
}

/**
* 
* @param mediaType
* The mediaType
*/
public void setMediaType(String mediaType) {
this.mediaType = mediaType;
}

/**
* 
* @return
* The accessURL
*/
public String getAccessURL() {
return accessURL;
}

/**
* 
* @param accessURL
* The accessURL
*/
public void setAccessURL(String accessURL) {
this.accessURL = accessURL;
}

/**
* 
* @return
* The downloadURL
*/
public String getDownloadURL() {
return downloadURL;
}

/**
* 
* @param downloadURL
* The downloadURL
*/
public void setDownloadURL(String downloadURL) {
this.downloadURL = downloadURL;
}

public Map<String, Object> getAdditionalProperties() {
return this.additionalProperties;
}

public void setAdditionalProperty(String name, Object value) {
this.additionalProperties.put(name, value);
}

}
