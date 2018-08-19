package com.openworldsimulator.tools;

import com.openworldsimulator.demographics.DemographicParams;
import com.openworldsimulator.model.Population;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class PopulationLoaderTest {
    @Test
    public void testLoad() throws IOException {
        int requested = 10000;
        Population population = new Population();

        InitialPopulationLoader loader = new InitialPopulationLoader(
                population,
                new DemographicParams(),
                "Spain", 2015, requested);

        loader.load();

        Assert.assertTrue(Math.abs(population.size() - requested) <= requested * 0.05);
    }
}
