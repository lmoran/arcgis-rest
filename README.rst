ArcGIS ReST API DataStore
=========================

Overview
--------

This datastore implements a very limited portion of the ArcGIS ReST API 
(http://resources.arcgis.com/en/help/arcgis-rest-api/), which is supported by both ArcGIS Server 
and ArcGIS Online. 

Specifically, only the FeatureServer services on either ArcGIS Online (Open Data) or 
ArcGIS Server folders are covered so far.

The main parmater is the API URL, which can take the form of:
* ArcGIS Online (Open Data): http://data.dhs.opendata.arcgis.com/data.json  
* ArcGIS folder: http://sampleserver1.arcgisonline.com/ArcGIS/rest/services/Demographics


Requirements
------------

ArcGIS ReST API 10.41


Functionality
-------------

Currently, only a part of the Feature Service (http://resources.arcgis.com/en/help/arcgis-rest-api/#/Feature_Service/02r3000000z2000000/)
is implemented, which allows to:

* It can etrieve a list of avaialable layers
* It can query a layer by bounding box


Test
----

To test for memory leaks (and general performance), one coudl use ApacheBenchmark, as in:

```
#!/bin/bash
HOST=localhost:8080
NUSER=20
NREQ=20
TIMEOUT=90
LAYER="cite:LGAProfiles2014Beta"
URL="${HOST}/geoserver/wfs?service=WFS&version=1.1.0&request=GetFeature\
&typeName=${LAYER}\
&outputFormat=application%2Fjson"
ab -n $((${NREQ} * ${NUSER})) -c ${NUSER} -s ${TIMEOUT} ${URL}
```