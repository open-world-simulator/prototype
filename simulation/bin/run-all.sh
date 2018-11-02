set -e

#!/usr/bin/env bash
./ows.sh Spain-0 Spain.defaults months=1200 year=2017 output=../simulation/output-sim INITIAL_POPULATION_SIZE=10000
./ows.sh Spain-1 Spain.defaults months=1200 year=2017 output=../simulation/output-sim INITIAL_POPULATION_SIZE=10000 MIGRATION_INFLOW_BASE_PCT=0.5
./ows.sh Spain-2 Spain.defaults months=1200 year=2017 output=../simulation/output-sim INITIAL_POPULATION_SIZE=10000 MIGRATION_INFLOW_BASE_PCT=1
./ows.sh Spain-3 Spain.defaults months=1200 year=2017 output=../simulation/output-sim INITIAL_POPULATION_SIZE=10000 MIGRATION_INFLOW_BASE_PCT=1.5

#echo "Running with higher fertility rates"
#./ows.sh Spain-1_a Spain.defaults months=1200 year=2017 output=../simulation/output-sim INITIAL_POPULATION_SIZE=10000 MIGRATION_INFLOW_BASE_PCT=0.5 MATERNITY_NUM_CHILDREN_MEAN=2.5
#./ows.sh Spain-2_a Spain.defaults months=1200 year=2017 output=../simulation/output-sim INITIAL_POPULATION_SIZE=10000 MIGRATION_INFLOW_BASE_PCT=1 MATERNITY_NUM_CHILDREN_MEAN=2.5
#./ows.sh Spain-3_a Spain.defaults months=1200 year=2017 output=../simulation/output-sim INITIAL_POPULATION_SIZE=10000 MIGRATION_INFLOW_BASE_PCT=1.5 MATERNITY_NUM_CHILDREN_MEAN=2.5

