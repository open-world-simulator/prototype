package com.openworldsimulator.demographics;

import com.openworldsimulator.experiments.Experiment;
import com.openworldsimulator.experiments.ExperimentResult;
import com.openworldsimulator.experiments.ExperimentsManager;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestDemographics {
    @Test
    public void testSimpleExperiment() throws Exception {
        File baseDir = new File(".", "output-tests");

        ExperimentsManager experimentsManager = new ExperimentsManager(
                baseDir
        );

        Map props = new HashMap();
        props.put("_ENABLE_ECONOMY_SIMULATION", "0");
        props.put("INITIAL_POPULATION_SIZE", 10000);
        props.put("MIGRATION_INFLOW_BASE_PCT", "1.0");

        Experiment e = experimentsManager.newExperiment();
        e.setExperimentId("demographics-run-1");
        e.setBaseSimulationConfig("Narnia.defaults");
        e.setMonths(30*12);
        e.setBaseYear(2017);
        e.setOptionalProperties(props);

        experimentsManager.saveExperiment(e);
        e.run();

        List<String> results = experimentsManager.listExperimentResults(e.getExperimentId());
        Assert.assertTrue(results.size() > 10);
        results.forEach(System.out::println);
    }
}
