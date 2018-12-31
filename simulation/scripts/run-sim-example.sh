#!/usr/bin/env bash
set -e

POPULATION_SIZE=1000

./ows.sh Test-1 Narnia.defaults months=600 year=2017 output=../simulation/output-sim \
    INITIAL_POPULATION_SIZE=$POPULATION_SIZE \
    MIGRATION_INFLOW_BASE_PCT=1 \g
    MIGRATION_OUTFLOW_BASE_PCT=1
