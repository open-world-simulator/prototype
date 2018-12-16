package com.openworldsimulator.demographics;

import com.openworldsimulator.simulation.ModelParameters;

public class DemographicParams extends ModelParameters {

    public String INITIAL_DEMOGRAPHY_DATA_COUNTRY;

    public double INITIAL_LIFE_EXPECTANCY_MEAN = 80;
    public double INITIAL_LIFE_EXPECTANCY_STDEV = 10;
    public double INITIAL_LIFE_EXPECTANCY_MAX = 120;

    public double INITIAL_POPULATION_SIZE = 1000;

    /*
     * Reproductive behaviour
     */
    public double MATERNITY_AGE_MEAN = 30;
    public double MATERNITY_AGE_STDEV = 5;
    public double MATERNITY_NUM_CHILDREN_MEAN = 2.5;
    public double MATERNITY_NUM_CHILDREN_STDEV = 1;
    public double MATERNITY_MIN_AGE = 14;
    public double MATERNITY_MAX_AGE = 50;

    /*
     * Immigration flow
     */
    public double MIGRATION_INFLOW_BASE_PCT  = 0; // Base percentage of initial population
    public double MIGRATION_INFLOW_AGE_MEAN  = 0; // Mean age of immigrants
    public double MIGRATION_INFLOW_AGE_STDEV = 0;
    public double MIGRATION_INFLOW_GENDER_DIST = 0.55; // Male gender bias

    /*
     * Emigration flow
     */
    public double MIGRATION_OUTFLOW_BASE_PCT  = 0;    // Base percentage of initial population
    public double MIGRATION_OUTFLOW_AGE_MEAN  = 25;   // Mean age of emmigrants
    public double MIGRATION_OUTFLOW_AGE_STDEV = 5;


    public DemographicParams() {
    }
}
