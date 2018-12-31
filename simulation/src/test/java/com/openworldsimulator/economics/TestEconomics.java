package com.openworldsimulator.economics;

import com.openworldsimulator.experiments.Experiment;
import com.openworldsimulator.experiments.ExperimentsManager;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestEconomics {

    @Test
    public void testMicroeconomy() throws Exception {
        File baseDir = new File(".", "output-tests");

        ExperimentsManager experimentsManager = new ExperimentsManager(
                baseDir
        );

        Map props = new HashMap();
        props.put("_ENABLE_ECONOMY_SIMULATION", "1");
        props.put("INITIAL_POPULATION_SIZE", "1000");

        Experiment e = experimentsManager.newExperiment();
        e.setExperimentId("economics-1");
        e.setBaseSimulationConfig("Narnia.defaults");
        e.setMonths(20 * 12);
        e.setBaseYear(2017);
        e.setOptionalProperties(props);

        experimentsManager.saveExperiment(e);

        e.getSimulation().getSimulationLog().setDebug(true);

        e.run();

        List<String> results = experimentsManager.listExperimentResults(e.getExperimentId());
        Assert.assertTrue(results.size() > 10);
        results.forEach(System.out::println);
    }

    @Test
    public void testNarnia() throws Exception {
        File baseDir = new File(".", "output-tests");

        ExperimentsManager experimentsManager = new ExperimentsManager(
                baseDir
        );

        Map props = new HashMap();
        props.put("_ENABLE_ECONOMY_SIMULATION", "1");
        props.put("INITIAL_POPULATION_SIZE", "10000");
        props.put("MIGRATION_INFLOW_BASE_PCT", "0.5");
        props.put("EVOLVE_TOTAL_PCT_MATERNITY_NUM_CHILDREN_MEAN", "50");
        props.put("EVOLVE_TOTAL_PCT_MATERNITY_AGE_MEAN", "-10");

        Experiment e = experimentsManager.newExperiment();
        e.setExperimentId("economics-2");
        e.setBaseSimulationConfig("Narnia.defaults");
        e.setMonths(40 * 12);
        e.setBaseYear(2017);
        e.setOptionalProperties(props);

        experimentsManager.saveExperiment(e);

        e.getSimulation().getSimulationLog().setDebug(true);

        e.run();

        List<String> results = experimentsManager.listExperimentResults(e.getExperimentId());
        Assert.assertTrue(results.size() > 10);
        results.forEach(System.out::println);
    }



}