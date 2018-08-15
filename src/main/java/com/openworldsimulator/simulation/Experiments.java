package com.openworldsimulator.simulation;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class Experiments {

    public Experiments() {
    }

    public void runSuite(File outputPath, String suiteId, int months) throws IOException {
        List<String> experimentIds = new ArrayList<>();
        Properties properties = loadSuiteConfig(suiteId);
        properties.keySet().forEach(k -> experimentIds.add(k.toString()));

        experimentIds.forEach(e -> {
            try {
                if (e.trim().length() > 0 && !e.startsWith("#")) {
                    runExperiment(outputPath, e, months);
                }
            } catch (Exception e1) {
                System.out.println("Error running experiment " + e);
                e1.printStackTrace();
            }
        });
    }

    public void runExperiment(File outputPath, String experiment, int months) throws Exception {

        System.out.println("*****************************************************************************************");
        System.out.println("Writing experiments output to " + outputPath);


        // Load experiment config
        Properties properties = loadExperimentConfig(experiment);
        String id = properties.getProperty("experiment.id");
        String description = properties.getProperty("experiment.description");

        if (id == null) {
            System.out.println("[ERROR] No ID configured for experiment " + experiment);
        } else {
            File experimentDir = new File(outputPath, id);
            System.out.println("Cleaning " + outputPath);
            if (outputPath.getPath().length() <= 1) {
                throw new IOException("Output path seems wrong - cancelling just in case : " + outputPath.getPath());
            }

            if(experimentDir.exists() ) {
                FileUtils.cleanDirectory(new File(outputPath, id));
            }

            Simulation simulation = new Simulation(
                    id,
                    outputPath);

            simulation.init(properties);
            simulation.buildDefaultConfig(properties);

            simulation.log("Running experiment: ");
            simulation.log("- ID  : " + id);
            simulation.log("- DESC: " + description);
            simulation.log("- TIME: " + new Date());

            simulation.simulate(months);
        }
    }


    private Properties loadExperimentConfig(String experiment) throws IOException {
        Properties configProperties = new Properties();
        String resourceName = "experiments/" + experiment + ".experiment";
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
        if (in == null) {
            throw new FileNotFoundException("Resource " + resourceName + " not found in classpath");
        }
        configProperties.load(in);

        return configProperties;
    }

    private Properties loadSuiteConfig(String suite) throws IOException {
        Properties configProperties = new Properties();
        configProperties.load(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("experiments/" + suite + ".suite"));

        return configProperties;
    }
}
