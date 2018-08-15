package com.openworldsimulator.demographics;

import com.openworldsimulator.simulation.ModelParameters;
import com.openworldsimulator.tools.ModelParametersTools;

public class DemographicParams implements ModelParameters {

    public double INITIAL_LIFE_EXPECTANCY_MEAN = 0;
    public double INITIAL_LIFE_EXPECTANCY_STDEV = 0;
    public double INITIAL_LIFE_EXPECTANCY_MAX = 0;
    public double INITIAL_POPULATION_SIZE = 0;

    /*
     * Reproductive behaviour
     */
    public double MATERNITY_AGE_MEAN = 0;
    public double MATERNITY_AGE_STDEV = 0;
    public double MATERNITY_NUM_CHILDREN_MEAN = 0;
    public double MATERNITY_NUM_CHILDREN_STDEV = 0;
    public double MATERNITY_MIN_AGE = 0;
    public double MATERNITY_MAX_AGE = 0;

    /*
     * TODO: Inmigration flow
     */

    /*
     * TODO: Emigration flow
     */

    public DemographicParams() {
    }

    @Override
    public String toString() {
        return ModelParametersTools.toString(this);
    }


}
