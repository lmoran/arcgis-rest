#!/bin/bash

# OLD: 18m with 40MB heap used
# NEW: 17m35s with 60MB heap used
#HOST=130.56.253.19
HOST=localhost:8080
NUSER=2
NREQ=10
URL="${HOST}/geoserver/wfs?service=WFS&version=1.0.0&request=GetFeature\
&typeName=cite:LGAProfiles2014Beta\
&outputFormat=application%2Fjson"
ab -n $((${NREQ} * ${NUSER})) -c ${NUSER} ${URL}

