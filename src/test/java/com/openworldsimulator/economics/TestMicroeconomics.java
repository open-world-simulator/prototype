package com.openworldsimulator.economics;

import com.openworldsimulator.model.Person;
import com.openworldsimulator.simulation.Simulation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class TestMicroeconomics {

    private Simulation simulation;

    protected void buildTestSimulation() throws IOException {
        File output = new File(".", "output");

        System.out.println("Output at " + output.getCanonicalPath());

        Assert.assertTrue(output.exists());

        simulation = new Simulation("test-economics", output);

        Properties properties = new Properties();
        properties.put("INITIAL_POPULATION_SIZE", "1");

        simulation.init(properties);
        simulation.buildDefaultConfig(properties);
    }

    protected Person getSinglePerson() {
        return simulation.getPopulation().getPeople().get(0);
    }

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testMicroeconomy() throws Exception {
        System.out.println("TEST");

        System.out.println("Preparing runSimulation...");
        buildTestSimulation();
        simulation.simulate(1);
        System.out.println("Done...");

        Person p = getSinglePerson();


        System.out.println(p);

    }
}