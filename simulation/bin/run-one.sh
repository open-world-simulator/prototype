#!/usr/bin/env bash
set -e

./ows.sh Narnia-1 Narnia.defaults months=1200 year=2017 output=../simulation/output-sim INITIAL_POPULATION_SIZE=10000 MIGRATION_INFLOW_BASE_PCT=0.5

