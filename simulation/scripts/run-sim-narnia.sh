#!/usr/bin/env bash
set -e

POPULATION_SIZE=50000
MONTHS=720
YEAR=2018

./ows.sh Narnia-0 Narnia.defaults months=${MONTHS} year=${YEAR} output=../simulation/output-sim \
    INITIAL_POPULATION_SIZE=$POPULATION_SIZE \
    MIGRATION_INFLOW_BASE_PCT=0   \
    MIGRATION_OUTFLOW_BASE_PCT=0

./ows.sh Narnia-1 Narnia.defaults months=${MONTHS} year=${YEAR} output=../simulation/output-sim \
    INITIAL_POPULATION_SIZE=$POPULATION_SIZE \
    MIGRATION_INFLOW_BASE_PCT=0.5 \
    MIGRATION_OUTFLOW_BASE_PCT=0

./ows.sh Narnia-2 Narnia.defaults months=${MONTHS} year=${YEAR} output=../simulation/output-sim \
    INITIAL_POPULATION_SIZE=$POPULATION_SIZE \
    MIGRATION_INFLOW_BASE_PCT=0.75   \
    MIGRATION_OUTFLOW_BASE_PCT=0

./ows.sh Narnia-1.1 Narnia.defaults months=${MONTHS} year=${YEAR} output=../simulation/output-sim \
    INITIAL_POPULATION_SIZE=$POPULATION_SIZE \
    MIGRATION_INFLOW_BASE_PCT=0.5   \
    MIGRATION_OUTFLOW_BASE_PCT=0 \
    EVOLVE_TOTAL_PCT_MATERNITY_NUM_CHILDREN_MEAN=30 \
    EVOLVE_TOTAL_PCT_MATERNITY_AGE_MEAN=-20.0


#./ows.sh Narnia-3 Narnia.defaults months=1200 year=2017 output=../simulation/output-sim \
#    INITIAL_POPULATION_SIZE=$POPULATION_SIZE \
#    MIGRATION_INFLOW_BASE_PCT=1.5 \
#    MIGRATION_OUTFLOW_BASE_PCT=1

#echo "Running with higher fertility rates"
#./ows.sh Narnia-1_a Narnia.defaults months=1200 year=2017 output=../simulation/output-sim INITIAL_POPULATION_SIZE=10000 MIGRATION_INFLOW_BASE_PCT=0.5 MATERNITY_NUM_CHILDREN_MEAN=2.5
#./ows.sh Narnia-2_a Narnia.defaults months=1200 year=2017 output=../simulation/output-sim INITIAL_POPULATION_SIZE=10000 MIGRATION_INFLOW_BASE_PCT=1 MATERNITY_NUM_CHILDREN_MEAN=2.5
#./ows.sh Narnia-3_a Narnia.defaults months=1200 year=2017 output=../simulation/output-sim INITIAL_POPULATION_SIZE=10000 MIGRATION_INFLOW_BASE_PCT=1.5 MATERNITY_NUM_CHILDREN_MEAN=2.5

