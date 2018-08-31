package com.openworldsimulator.experiments;

import com.openworldsimulator.simulation.Simulation;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Experiment {
    private String experimentId;
    private String baseSimulationConfig;
    private int months;
    private int baseYear;
    private Map optionalProperties;
    private transient Simulation simulation;
    private transient ExperimentsManager experimentsManager;

    public Experiment(
            int baseYear,
            int nMonths,
            ExperimentsManager experimentsManager,
            String experimentId, String baseConfig, Map optionalProperties) {
        setExperimentId(experimentId);
        setMonths(nMonths);
        setBaseYear(baseYear);
        setBaseSimulationConfig(baseConfig);
        setExperimentsManager(experimentsManager);
        setOptionalProperties(optionalProperties);
    }

    public Simulation getSimulation() throws Exception {
        if (simulation == null) {
            simulation = new Simulation(
                    experimentId,
                    experimentsManager.getExperimentDirectory(experimentId)
            );

            simulation.init();
            simulation.loadDefaultConfig(baseYear, months, baseSimulationConfig, optionalProperties);
        }
        return simulation;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public String getExperimentId() {
        return experimentId;
    }

    public boolean isValidId() {
        if (experimentId == null || experimentId.length() < 3) return false;
        for (int i = 0; i < experimentId.length(); i++) {
            char c = experimentId.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '-') return false;
        }
        return true;
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
        if (optionalProperties == null) {
            this.optionalProperties = new HashMap();
        } else {
            this.optionalProperties = optionalProperties;
        }
        simulation = null;
    }

    public int getMonths() {
        return months;
    }

    public void setMonths(int months) {
        this.months = months;
    }

    public int getBaseYear() {
        return baseYear;
    }

    public void setBaseYear(int baseYear) {
        this.baseYear = baseYear;
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
        simulation.log("ID     : " + experimentId);
        simulation.log("BASE   : " + baseSimulationConfig);
        simulation.log("TIME   : " + new Date());
        simulation.log("YEAR   : " + baseYear);
        simulation.log("MONTHS : " + months);

        try {
            simulation.init();
            simulation.loadDefaultConfig(baseYear, months, baseSimulationConfig, optionalProperties);
            simulation.simulate();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
