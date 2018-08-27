package com.openworldsimulator.experiments;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class TestExperimentsManager {

    @Test
    public void testWrongDirectory() {

        boolean exceptionThrown = false;
        try {
            new ExperimentsManager(
                    new File(".")
            );
        } catch (IOException e) {
            // Exception is expected
            Assert.assertTrue(e.getMessage().contains(ExperimentsManager.MARKER_FILE));
            exceptionThrown = true;
        }

        Assert.assertTrue(exceptionThrown);
    }

    @Test
    public void testLifeCycle() throws Exception {
        File baseDir = new File(".", "output-tests");

        ExperimentsManager experimentsManager = new ExperimentsManager(
                baseDir
        );

        experimentsManager.deleteAllExperiments();

        Assert.assertEquals(0, experimentsManager.listExperiments().size());

        Experiment e = new Experiment(
                experimentsManager,
                "experiment1",
                "blank",
                null,
                1000
        );

        File d = experimentsManager.getExperimentDirectory(e.getExperimentId());
        Assert.assertTrue(d.getPath().endsWith("/" + e.getExperimentId()));

        experimentsManager.saveExperiment(e);
        Assert.assertTrue(experimentsManager.getExperimentDirectory(e.getExperimentId()).exists());
        Assert.assertNotNull(experimentsManager.loadExperiment(e.getExperimentId()));
        Assert.assertEquals(
                experimentsManager.getExperimentDirectory(e.getExperimentId()),
                e.getSimulation().getSimulationOutputPath());


        experimentsManager.deleteExperiment(e.getExperimentId());
        Assert.assertEquals(0, experimentsManager.listExperiments().size());

        for (int i = 0; i < 10; i++) {
            e = new Experiment(
                    experimentsManager,
                    "experiment" + i,
                    "blank",
                    null,
                    1000 + i
            );
            experimentsManager.saveExperiment(e);
        }

        Assert.assertEquals(10, experimentsManager.listExperiments().size());
        for (int i = 0; i < 10; i++) {
            e = experimentsManager.loadExperiment("experiment" + i);
            // Check all fields correctly loaded
            Assert.assertEquals(e.getExperimentId(), "experiment" + i);
            Assert.assertEquals(e.getMonths(), 1000 + i);
        }

        experimentsManager.deleteAllExperiments();

        Assert.assertEquals(0, experimentsManager.listExperiments().size());
    }
}
