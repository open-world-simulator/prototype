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
        props.put("INITIAL_DEMOGRAPHY_DATA_COUNTRY", "Spain");
        props.put("INITIAL_DEMOGRAPHY_DATA_YEAR", "2017");
        props.put("_ENABLE_ECONOMY_SIMULATION", "0");

        Experiment e = experimentsManager.newExperiment();
        e.setExperimentId("run-1");
        e.setBaseSimulationConfig("blank.defaults");
        e.setMonths(10);
        e.setOptionalProperties(props);

        experimentsManager.saveExperiment(e);
        e.run();

        List<String> results = experimentsManager.listExperimentResults(e.getExperimentId());
        Assert.assertTrue(results.size() > 10);
        results.forEach(System.out::println);
    }
}
