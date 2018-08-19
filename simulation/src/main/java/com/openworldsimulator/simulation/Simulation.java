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

    private Population population;
    private Banks banks;
    private Companies companies;
    private PublicSector publicSector;
    private Transactions transactions;
    private Map<String, Double> simulationParametersChangeRate = new HashMap<>();

    private SimulationLog simulationLog;
    private File globalOutputPath;

    private List<SimulationModel> models;

    public Simulation(String simulationId, File outputPath) {
        this.simulationId = simulationId;
        this.globalOutputPath = outputPath;
    }

    public String getSimulationId() {
        return simulationId;
    }

    public File getSimulationOutputPath() {
        return new File(globalOutputPath, getSimulationId());
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

    public void buildDefaultConfig(String defaultSettings, Map optionalProperties) throws IOException {

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
        DemographicsModel demographicsModel = new DemographicsModel(this, demographicParams, globalOutputPath);

        EconomyModel microEconomyModel = new EconomyModel(this, globalOutputPath, economicsParams);

        addSimulationModels(Arrays.asList(
                demographicsModel//,
                //microEconomyModel
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
        getSimulationOutputPath().mkdirs();

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

    public void simulate(int nMonths) throws Exception {

        log("* RUNNING SIMULATION : " + simulationId + " for " + nMonths + " months");

        // Do runSimulation initialization
        log("- Initializing models ");

        models.forEach(SimulationModel::init);

        // Write initial reports
        log("- Writing reports at initial status");
        models.forEach(m -> {
            if (m.getStats() != null) {
                m.getStats().writeChartsAtStart();
            }
        });

        for (int i = 1; i <= nMonths; i++) {
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
        log("- Writing reports at end status");
        models.forEach(m -> {
            if (m.getStats() != null) {
                logDebug("Writing reports at end" + m.getId());
                m.getStats().writeChartsAtEnd();
            }
        });
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
        log("Loading default config: " + config);
        configProperties.load(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("defaults/" + config + ".defaults")
        );
        return configProperties;
    }
}