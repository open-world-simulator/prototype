package com.openworldsimulator.simulation;

import com.openworldsimulator.demographics.DemographicParams;
import com.openworldsimulator.demographics.DemographicsModel;
import com.openworldsimulator.economics.EconomyModel;
import com.openworldsimulator.economics.EconomyParams;
import com.openworldsimulator.model.*;
import com.openworldsimulator.tools.ModelParametersTools;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Simulation {

    private String simulationId;

    private int currentMonth = 0;
    private boolean running = false;
    private String status = null;

    private Population population;
    private Banks banks;
    private Companies companies;
    private PublicSector publicSector;
    private Transactions transactions;
    private Map<String, Double> simulationParametersChangeRate = new HashMap<>();

    private File simulationOutputPath;
    private SimulationLog simulationLog;

    private List<SimulationModel> models;

    public Simulation(String simulationId, File simulationOutputPath) {
        this.simulationId = simulationId;
        this.simulationOutputPath = simulationOutputPath;
    }

    public String getSimulationId() {
        return simulationId;
    }

    public File getSimulationOutputPath() {
        return simulationOutputPath;
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

    public PublicSector getPublicSector() {
        return publicSector;
    }

    public Transactions getTransactions() {
        return transactions;
    }

    public void loadDefaultConfig(String defaultSettings, Map optionalProperties) throws IOException {

        // Load parameters defaults

        simulationLog.log("Loading parameters....");

        Properties defaults = loadDefaults(defaultSettings);

        DemographicParams demographicParams =
                ModelParametersTools.loadParameterValues(defaults, optionalProperties, new DemographicParams());

        simulationLog.log(demographicParams.toString());

        // Create economics model
        EconomyParams economicsParams =
                ModelParametersTools.loadParameterValues(defaults, optionalProperties, new EconomyParams());

        simulationLog.log(economicsParams.toString());

        // Load parameter change rate
        ModelParametersTools.loadParameterChanges(defaults, simulationParametersChangeRate);
        simulationLog.log(printParametersRate());

        simulationLog.log("Building models....");
        DemographicsModel demographicsModel = new DemographicsModel(this, demographicParams, simulationOutputPath);

        EconomyModel microEconomyModel = new EconomyModel(this, simulationOutputPath, economicsParams);

        addSimulationModels(Arrays.asList(
                demographicsModel,
                microEconomyModel
        ));
    }

    private String printParametersRate() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("Parameter change rate:\n");

        simulationParametersChangeRate.forEach(
                (k, v) -> {
                    buffer.append(k)
                            .append("=")
                            .append(String.format("%.02f", v))
                            .append(" - ")
                            .append(String.format("%.02f", v * 100.0))
                            .append("%\n");
                }
        );

        return buffer.toString();
    }

    public void init() throws IOException {
        // Create runSimulation log
        simulationLog = new SimulationLog(this, "runSimulation-" + getSimulationId());
        simulationLog.init();

        // Create population
        population = new Population();

        banks = new Banks();
        companies = new Companies();
        publicSector = new PublicSector();
        transactions = new Transactions(companies, publicSector, population, banks);
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

    public void simulate(int nMonths) throws Exception {

        try {
            setCurrentMonth(0);
            setRunning(true);

            log("* RUNNING SIMULATION : " + simulationId + " for " + nMonths + " months");

            // Do runSimulation initialization
            setStatus("Initializing Models");

            models.forEach(SimulationModel::init);

            setStatus("Writing initial reports");

            // Write initial reports
            models.forEach(m -> {
                if (m.getStats() != null) {
                    m.getStats().writeChartsAtStart();
                }
            });

            setStatus("Running");
            for (int i = 1; i <= nMonths; i++) {
                setCurrentMonth(1);
                logDebug("Iteration " + i);
                if (i % 120 == 0) {
                    log(" " + i + " (" + (i / 12) + " years)");
                    //System.out.println(getPublicSector().getBalanceSheet());
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
                        m.getStats().writeSnapshots(month);
                    }
                });
            }

            log("\n");

            // Do post-runSimulation stats collection
            setStatus("Writing reports at end status");
            models.forEach(m -> {
                if (m.getStats() != null) {
                    logDebug("Writing reports at end" + m.getId());
                    m.getStats().writeChartsAtEnd();
                }
            });
        }
        finally {
            setStatus(null);
            setRunning(false);
            setCurrentMonth(0);
        }
    }

    public void evolveParametersMonthly(ModelParameters parameters) {
        ModelParametersTools.evolveParameterDeltaMonthly(parameters, simulationParametersChangeRate);
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
            configProperties.load(
                    Thread.currentThread().getContextClassLoader().getResourceAsStream("defaults/" + config)
            );
        }
        return configProperties;
    }
}
