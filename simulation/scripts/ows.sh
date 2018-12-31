#!/bin/sh
#set -o xtrace
set -e
cd ..
export MAVEN_OPTS=-Xmx4096m
mvn clean install -DskipTests
#clear
ARGS="$@"
mvn exec:java -Dexec.mainClass="com.openworldsimulator.SimulationMain" -Dexec.args="${ARGS}"
