ArcGIS ReST API DataStore
=========================

Overview
--------

This datastore implements a very limited portion of the ArcGIS ReST API (http://resources.arcgis.com/en/help/arcgis-rest-api/), which is supported by both ArcGIS Server and ArcGIS Online. 


Requirements
------------

ArcGIS ReST API 10.41


Functionality
-------------

Currntly, only a part of the Feature Service (http://resources.arcgis.com/en/help/arcgis-rest-api/#/Feature_Service/02r3000000z2000000/) is implemented, which allows to:

* It can etrieve a list of avaialable layers (tables as well?)
* It can query a layer by bounding box