#!/bin/sh
set -o xtrace
set -e
cd ..
export MAVEN_OPTS=-Xmx4096m
mvn clean install -DskipTests
#clear
echo "$@"
mvn exec:java -Dexec.mainClass="com.openworldsimulator.SimulationMain" -Dexec.args="$1 $2 $3 $4 $5 $6 $7 $8 $9 $10"
