package com.openworldsimulator.simulation;

import com.openworldsimulator.model.Person;
import com.openworldsimulator.tools.ConfigTools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public abstract class SimulationModel {
    private int PARALLEL_POPULATION_SEGMENTS = ConfigTools.getConfigInt("PARALLEL_POPULATION_SEGMENTS", 8);

    protected Simulation simulation;
    private File outputPath;

    public SimulationModel(File outputPath, Simulation simulation) {
        this.outputPath = outputPath;
        this.simulation = simulation;
    }

    public void init() {
        // Check output path
        if (outputPath == null) {
            System.out.println("Output path is NULL");
            throw new ExceptionInInitializerError();
        }

        outputPath.mkdirs();
        if (!outputPath.exists() || !outputPath.isDirectory() || !outputPath.canWrite()) {
            System.out.println("Invalid output path or can't write to: " + outputPath.getPath());
            throw new ExceptionInInitializerError();
        }
    }

    protected void log(String log) {
        simulation.log(log);
    }

    protected void logDebug(String log) {
        simulation.logDebug(log);
    }

    protected void log(String format, Object... args) {
        simulation.log(format, args);
    }

    protected void logDebug(String format, Object... args) {
        simulation.logDebug(format, args);
    }


    /**
     * Splits a list of population segment and executes a calculation over each segment in parallel
     *
     * @param population
     * @param computation
     */
    protected void parallelRun(
            List<Person> population,
            Consumer<Person[]> computation
    ) {
        ExecutorService executorService = Executors.newWorkStealingPool();

        int segmentSize = population.size() / PARALLEL_POPULATION_SEGMENTS;

        List<Person[]> segments = new ArrayList<>();

        for (int i = 0; i < PARALLEL_POPULATION_SEGMENTS; i++) {
            int segmentBegin = i * segmentSize;
            int segmentEnd = i == PARALLEL_POPULATION_SEGMENTS - 1 ? population.size() : segmentBegin + segmentSize;
            int segmentLength = segmentEnd - segmentBegin;

            logDebug("Cutting segment " + i + " [" + segmentBegin + " to " + (segmentEnd - 1) + "] for population size of " + population.size());

            segments.add(population.subList(segmentBegin, segmentEnd).toArray(new Person[segmentLength]));
        }

        for (Person[] s : segments) {
            executorService.submit(
                    () -> {
                        try {
                            computation.accept(s);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
            );
        }
        try {
            executorService.shutdown();
            executorService.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public abstract String getId();

    public ModelStats[] getStats() {
        return null;
    }

    public abstract ModelParameters getParams();

    public abstract void preSimulation(int month);

    public abstract void runSimulation(int month);

    public abstract void postSimulation(int month);
}
