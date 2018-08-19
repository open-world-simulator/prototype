package com.openworldsimulator.simulation;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

public class Experiments {

    public Experiments() {
    }

    public void runExperiment(String experimentId, File outputPath, String baseConfig, Map optionalProperties, int months) throws Exception {

        System.out.println("*****************************************************************************************");
        File experimentDir = new File(outputPath, experimentId);
        System.out.println("Writing experiments output to " + experimentDir);
        System.out.println("Cleaning " + experimentDir);
        if (outputPath.getPath().length() <= 3) {
            throw new IOException("Output path seems wrong - cancelling : " + experimentDir);
        }

        if (experimentDir.exists()) {
            FileUtils.cleanDirectory(experimentDir);
        }

        Simulation simulation = new Simulation(
                experimentId,
                outputPath);

        simulation.init();
        simulation.buildDefaultConfig(baseConfig, optionalProperties);

        simulation.log("Running experiment at " + new Date() + ":");
        simulation.log("> ID     : " + experimentId);
        simulation.log("> BASE   : " + baseConfig);
        simulation.log("> TIME   : " + new Date());
        simulation.log("> MONTHS : " + months);

        simulation.simulate(months);
    }
}
