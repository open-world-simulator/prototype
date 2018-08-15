#!/bin/sh
cd ..
export MAVEN_OPTS=-Xmx2048m
mvn clean install -DskipTests
clear
mvn exec:java -Dexec.mainClass="com.openworldsimulator.SimulationMain" -Dexec.args="$1 $2 $3 $4"