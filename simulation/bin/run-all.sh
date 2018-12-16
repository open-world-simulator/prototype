#!/usr/bin/env bash
set -e

./ows.sh Narnia-0 Narnia.defaults months=1200 year=2017 output=../simulation/output-sim INITIAL_POPULATION_SIZE=100000
./ows.sh Narnia-1 Narnia.defaults months=1200 year=2017 output=../simulation/output-sim INITIAL_POPULATION_SIZE=100000 MIGRATION_INFLOW_BASE_PCT=0.5 MIGRATION_OUTFLOW_BASE_PCT=0.25
./ows.sh Narnia-2 Narnia.defaults months=1200 year=2017 output=../simulation/output-sim INITIAL_POPULATION_SIZE=100000 MIGRATION_INFLOW_BASE_PCT=1   MIGRATION_OUTFLOW_BASE_PCT=0.25
./ows.sh Narnia-3 Narnia.defaults months=1200 year=2017 output=../simulation/output-sim INITIAL_POPULATION_SIZE=100000 MIGRATION_INFLOW_BASE_PCT=1.5 MIGRATION_OUTFLOW_BASE_PCT=0.25

#echo "Running with higher fertility rates"
#./ows.sh Narnia-1_a Narnia.defaults months=1200 year=2017 output=../simulation/output-sim INITIAL_POPULATION_SIZE=10000 MIGRATION_INFLOW_BASE_PCT=0.5 MATERNITY_NUM_CHILDREN_MEAN=2.5
#./ows.sh Narnia-2_a Narnia.defaults months=1200 year=2017 output=../simulation/output-sim INITIAL_POPULATION_SIZE=10000 MIGRATION_INFLOW_BASE_PCT=1 MATERNITY_NUM_CHILDREN_MEAN=2.5
#./ows.sh Narnia-3_a Narnia.defaults months=1200 year=2017 output=../simulation/output-sim INITIAL_POPULATION_SIZE=10000 MIGRATION_INFLOW_BASE_PCT=1.5 MATERNITY_NUM_CHILDREN_MEAN=2.5

