package com.openworldsimulator.economics.params;

import com.openworldsimulator.simulation.ModelParameters;

public class PersonalEconomyParams extends ModelParameters {

    // Consumption params
    public double PROPENSITY_TO_CONSUMPTION_MEAN_RATE = 0.6;
    public double PROPENSITY_TO_CONSUMPTION_STDEV_RATE = 0.1;

    public double MONTHLY_NON_DISCRETIONARY_EXPENSES_MIN = 150;
    public double MONTHLY_NON_DISCRETIONARY_EXPENSES_MEAN = 400;
    public double MONTHLY_NON_DISCRETIONARY_EXPENSES_STDEV = 1;

    public double YIELD_SAVINGS_RATE = 0.03;
}
