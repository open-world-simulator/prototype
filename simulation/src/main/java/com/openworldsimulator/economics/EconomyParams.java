package com.openworldsimulator.economics;

import com.openworldsimulator.economics.params.*;
import com.openworldsimulator.simulation.ModelParameters;

import java.util.Map;
import java.util.Properties;

public class EconomyParams extends ModelParameters {
    public double _ENABLE_ECONOMY_SIMULATION = 1;

    public GovernmentParams government = new GovernmentParams();

    public JobMarketParams jobMarket = new JobMarketParams();

    public ConsumerParams personalEconomy = new ConsumerParams();

    @Override
    public void loadParameterValues(Properties defaultProperties, Map overrideProperties) {
        super.loadParameterValues(defaultProperties, overrideProperties);
        government.loadParameterValues(defaultProperties, overrideProperties);
        jobMarket.loadParameterValues(defaultProperties, overrideProperties);
        personalEconomy.loadParameterValues(defaultProperties, overrideProperties);
    }
}
