package com.openworldsimulator.simulation;

import java.io.File;

public abstract class SimulationModel {
    protected Simulation simulation;
    private File outputPath;

    public SimulationModel(File outputPath, Simulation simulation) {
        this.outputPath = outputPath;
        this.simulation = simulation;
    }

    public void init() {
        // Check output path
        if (outputPath == null) {
            throw new ExceptionInInitializerError("Output path must be configured");
        }

        outputPath.mkdirs();
        if (!outputPath.exists() || !outputPath.isDirectory() || !outputPath.canWrite()) {
            throw new ExceptionInInitializerError("Invalid output path or can't write to: " + outputPath.getPath());
        }
    }

    public void log(String log) {
        simulation.log(log);
    }

    public void logDebug(String log) {
        simulation.logDebug(log);
    }

    public void log(String format, Object... args) {
        simulation.log(format, args);
    }

    public void logDebug(String format, Object... args) {
        simulation.logDebug(format, args);
    }

    public ModelStats getStats() {
        return null;
    }

    public abstract String getId();

    public abstract void preSimulation(int month);

    public abstract void runSimulation(int month);

    public abstract void postSimulation(int month);
}
