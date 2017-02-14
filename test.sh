#!/bin/bash

#HOST=130.56.253.19
HOST=localhost:8080
NUSER=20
NREQ=100
TIMEOUT=90
URL="${HOST}/geoserver/wfs?service=WFS&version=1.1.0&request=GetFeature\
&typeName=cite:LGAProfiles2014Beta\
&outputFormat=application%2Fjson"
set -x
ab -n $((${NREQ} * ${NUSER})) -c ${NUSER} -s ${TIMEOUT} ${URL}

