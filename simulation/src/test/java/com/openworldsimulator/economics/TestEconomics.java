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
        e.setExperimentId("run-2");
        e.setBaseSimulationConfig("Spain.defaults");
        e.setMonths(20 * 12);
        e.setBaseYear(2017);
        e.setOptionalProperties(props);

        experimentsManager.saveExperiment(e);
        e.run();

        List<String> results = experimentsManager.listExperimentResults(e.getExperimentId());
        Assert.assertTrue(results.size() > 10);
        results.forEach(System.out::println);
    }

}