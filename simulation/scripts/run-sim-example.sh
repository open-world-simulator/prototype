#!/usr/bin/env bash
set -e

POPULATION_SIZE=50000
MONTHS=720
YEAR=2018

./ows.sh Test-1 Narnia.defaults months=${MONTHS} year=${YEAR} output=../simulation/output-sim \
    INITIAL_POPULATION_SIZE=$POPULATION_SIZE \
    MIGRATION_INFLOW_BASE_PCT=1.0   \
    MIGRATION_OUTFLOW_BASE_PCT=0