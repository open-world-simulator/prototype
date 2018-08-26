package com.openworldsimulator.experiments;

import com.openworldsimulator.simulation.Simulation;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

public class Experiment {
    public static final String DEFAULT_BASE_CONFIG = "blank";
    private String experimentId;
    private String baseSimulationConfig = DEFAULT_BASE_CONFIG;
    private int months;
    private Map optionalProperties;
    private transient Simulation simulation;
    private transient ExperimentsManager experimentsManager;

    public Experiment(
            ExperimentsManager experimentsManager,
            String experimentId, String baseConfig, Map optionalProperties, int months) {
        this.experimentId = experimentId;
        this.baseSimulationConfig = baseConfig;
        this.optionalProperties = optionalProperties;
        this.months = months;
        this.experimentsManager = experimentsManager;
    }

    public Simulation getSimulation() throws Exception {
        if (simulation == null) {
            simulation = new Simulation(
                    experimentId,
                    experimentsManager.getExperimentDirectory(experimentId)
            );

            simulation.init();
            simulation.loadDefaultConfig(baseSimulationConfig, optionalProperties);
        }
        return simulation;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public String getExperimentId() {
        return experimentId;
    }

    public String getBaseSimulationConfig() {
        return baseSimulationConfig;
    }

    public void setBaseSimulationConfig(String baseSimulationConfig) {
        this.baseSimulationConfig = baseSimulationConfig;
        simulation = null;
    }

    public Map getOptionalProperties() {
        return optionalProperties;
    }


    public void setOptionalProperties(Map optionalProperties) {
        this.optionalProperties = optionalProperties;
        simulation = null;
    }

    public int getMonths() {
        return months;
    }

    public void setMonths(int months) {
        this.months = months;
    }

    public ExperimentsManager getExperimentsManager() {
        return experimentsManager;
    }

    public void setExperimentsManager(ExperimentsManager experimentsManager) {
        this.experimentsManager = experimentsManager;
    }

    public void run() throws Exception {

        Simulation simulation = getSimulation();

        simulation.log("Running experiment at " + new Date() + ":");
        simulation.log("> ID     : " + experimentId);
        simulation.log("> BASE   : " + baseSimulationConfig);
        simulation.log("> TIME   : " + new Date());
        simulation.log("> MONTHS : " + months);

        simulation.simulate(months);
    }
}
