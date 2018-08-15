package com.openworldsimulator.simulation;

import java.io.File;
import java.io.IOException;

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

    /*
     * Reporting
     *
     */
    protected File getSimulationPath() {
        String dir = outputPath.getAbsolutePath();

        File fullPath = new File(dir, simulation.getSimulationId());
        fullPath.mkdirs();
        return fullPath;
    }

    public abstract String getId();

    public abstract void preSimulation(int month);

    public abstract void runSimulation(int month);

    public abstract void postSimulation(int month);

    public ModelStats getStats() {
        return null;
    }
}
