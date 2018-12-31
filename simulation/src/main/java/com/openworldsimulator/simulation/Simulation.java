package com.openworldsimulator.simulation;

import com.openworldsimulator.demographics.DemographicParams;
import com.openworldsimulator.demographics.DemographicsModel;
import com.openworldsimulator.economics.EconomyModel;
import com.openworldsimulator.economics.EconomyParams;
import com.openworldsimulator.model.*;
import com.openworldsimulator.simulation.stats.ParameterEvolutionStats;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Simulation {

    private String simulationId;

    private int baseYear = 0;
    private int totalMonths = 0;

    /////////////////////////////////////////////////////////////////////////7
    //// Status variables
    private volatile int currentMonth = 0;
    private volatile boolean running = false;
    private volatile String status = null;

    /////////////////////////////////////////////////////////////////////////7
    //// Simulation models
    private Population population;
    private Banks banks;
    private Companies companies;
    private Government government;
    private Transactions transactions;

    private ModelParametersEvolution modelParametersEvolution;
    private ParameterEvolutionStats evolutionStats;

    private File simulationOutputPath;
    private SimulationLog simulationLog;

    private List<SimulationModel> models;

    public Simulation(String simulationId, File simulationOutputPath) {
        this.simulationId = simulationId;
        this.simulationOutputPath = simulationOutputPath;
    }

    public SimulationLog getSimulationLog() {
        return simulationLog;
    }

    public String getSimulationId() {
        return simulationId;
    }

    public File getSimulationOutputPath() {
        return simulationOutputPath;
    }

    public ModelParametersEvolution getModelParametersEvolution() {
        return modelParametersEvolution;
    }

    public Population getPopulation() {
        return population;
    }

    public Banks getBanks() {
        return banks;
    }

    public Companies getCompanies() {
        return companies;
    }

    public Government getGovernment() {
        return government;
    }

    public Transactions getTransactions() {
        return transactions;
    }

    public void loadDefaultConfig(int year, int months, String defaultSettings, Map optionalProperties) throws IOException {

        this.baseYear = year;
        this.totalMonths = months;

        // Load parameters defaults

        simulationLog.log("Loading parameters....");

        Properties defaults = loadDefaults(defaultSettings);

        DemographicParams demographicParams = new DemographicParams();
        demographicParams.loadParameterValues(defaults, optionalProperties);

        simulationLog.log(demographicParams.toString());

        // Create economics model
        EconomyParams economicsParams = new EconomyParams();
        economicsParams.loadParameterValues(defaults, optionalProperties);

        simulationLog.log(economicsParams.toString());

        modelParametersEvolution = new ModelParametersEvolution(this);
        modelParametersEvolution.loadParametersChangeRate(defaults, optionalProperties);


        evolutionStats = new ParameterEvolutionStats(this);


        simulationLog.log("Building models....");
        DemographicsModel demographicsModel = new DemographicsModel(this, demographicParams, simulationOutputPath);

        EconomyModel microEconomyModel = new EconomyModel(this, simulationOutputPath, economicsParams);

        addSimulationModels(Arrays.asList(
                demographicsModel,
                microEconomyModel
        ));
    }

    public void init() throws IOException {
        // Create runSimulation log
        simulationLog = new SimulationLog(this, "runSimulation-" + getSimulationId());
        simulationLog.init();

        // Create population
        population = new Population();

        banks = new Banks();
        companies = new Companies();
        government = new Government();
        transactions = new Transactions(this);
    }

    public void addSimulationModels(List<SimulationModel> models) {
        this.models = new ArrayList<>();
        this.models.addAll(models);
    }

    public List<SimulationModel> getModels() {
        return models;
    }

    public SimulationModel getModel(String id) {
        for (SimulationModel m : getModels()) {
            if (m.getId().equals(id)) return m;
        }
        return null;
    }

    public int getBaseYear() {
        return baseYear;
    }

    public int getTotalMonths() {
        return totalMonths;
    }

    public int getCurrentMonth() {
        return currentMonth;
    }

    private void setCurrentMonth(int currentMonth) {
        this.currentMonth = currentMonth;
    }

    private void setRunning(boolean running) {
        this.running = running;
    }

    private void setStatus(String status) {
        this.status = status;
    }

    public boolean isRunning() {
        return running;
    }

    public void simulate() throws Exception {
        assert totalMonths > 0;
        assert baseYear > 0;

        try {
            setCurrentMonth(0);
            setRunning(true);

            log("Running simulation " + simulationId + " for " + totalMonths + " months from year " + baseYear);

            // Do runSimulation initialization
            setStatus("Initializing Models");

            models.forEach(SimulationModel::init);

            setStatus("Writing initial reports");

            // Write initial reports
            models.forEach(m -> {
                if (m.getStats() != null) {
                    for (ModelStats stats : m.getStats()) {
                        stats.writeChartsAtStart();
                    }
                }
            });

            setStatus("Running");
            for (int i = 1; i <= totalMonths; i++) {
                setCurrentMonth(i);
                logDebug("Iteration " + i);
                if (i % 120 == 0) {
                    log(" " + i + " (" + (i / 12) + " years)");
                    //System.out.println(getGovernment().getBalanceSheet());
                    //System.out.println(getCompanies().getBalanceSheet());
                }
                final int month = i;

                // Run simulation for all models
                models.forEach(m -> {
                    logDebug("Running simulation " + m.getId());
                    m.preSimulation(month);
                });

                // Run simulation for all models
                models.forEach(m -> {
                    logDebug("Running simulation " + m.getId());
                    m.runSimulation(month);
                });

                // Collect statistics
                models.forEach(m -> {
                    logDebug("Collecting statistics " + m.getId());
                    m.postSimulation(month);
                });

                // Write snapshots
                models.forEach(m -> {
                    logDebug("Writing snapshots " + m.getId());
                    if (m.getStats() != null) {
                        for (ModelStats stats : m.getStats()) {
                            stats.writeSnapshots(month);
                        }
                    }
                });

                // Evolve params
                models.forEach(m -> {
                    logDebug("Evolving params " + m.getId());
                    modelParametersEvolution.evolveMonthly(m.getParams(), month);
                });

                // Collect month stats
                evolutionStats.collect(month);
            }

            log("\n");

            // Do post-runSimulation stats collection
            setStatus("Writing reports at end status");
            models.forEach(m -> {
                if (m.getStats() != null) {
                    logDebug("Writing reports at end" + m.getId());
                    for (ModelStats stats : m.getStats()) {
                        stats.writeChartsAtEnd();
                    }
                }
            });

            evolutionStats.writeChartsAtEnd();
        } finally {
            setStatus(null);
            setRunning(false);
            setCurrentMonth(0);
        }
    }

    public void log(String log) {
        simulationLog.log(log);
    }

    public void logDebug(String log) {
        simulationLog.logDebug(log);
    }

    public void log(String format, Object... args) {
        simulationLog.log(format, args);
    }

    public void logDebug(String format, Object... args) {
        simulationLog.logDebug(format, args);
    }

    private Properties loadDefaults(String config) throws IOException {
        Properties configProperties = new Properties();
        if (config != null) {
            log("Loading default config: " + config);
            InputStream is = Simulation.class.getClassLoader().getResourceAsStream("defaults/" + config);
            if (is == null) {
                System.out.println("Can't find base resource: " + config);
                throw new FileNotFoundException("Resource " + config + " not found");
            }
            configProperties.load(is);
        }
        return configProperties;
    }
}
