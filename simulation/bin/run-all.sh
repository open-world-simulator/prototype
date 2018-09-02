#!/usr/bin/env bash
./ows.sh Spain-0 Spain.defaults months=1200 year=2017 output=../simulation/output-sim INITIAL_POPULATION_SIZE=100000
./ows.sh Spain-1 Spain.defaults months=1200 year=2017 output=../simulation/output-sim INITIAL_POPULATION_SIZE=100000 MIGRATION_INFLOW_PCT=0.5
./ows.sh Spain-2 Spain.defaults months=1200 year=2017 output=../simulation/output-sim INITIAL_POPULATION_SIZE=100000 MIGRATION_INFLOW_PCT=1
./ows.sh Spain-3 Spain.defaults months=1200 year=2017 output=../simulation/output-sim INITIAL_POPULATION_SIZE=100000 MIGRATION_INFLOW_PCT=1.5
