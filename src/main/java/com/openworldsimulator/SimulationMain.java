package com.openworldsimulator;

import com.openworldsimulator.simulation.Experiments;

import java.io.File;
import java.io.IOException;

public class SimulationMain {
    public static final int DEFAULT_MONTHS = 100 * 12;
    public static final String DEFAULT_EXPERIMENT = "default";

    private static boolean existSetting(String name, String[] args) {
        for (String arg : args) {
            if (arg.startsWith("-" + name)) {
                return true;
            }
        }
        return false;
    }

    private static String getParameter(String name, String[] args, String defaultValue) {
        for (String arg : args) {
            if (arg.contains("=")) {
                String key = arg.substring(0, arg.indexOf("="));
                String value = arg.substring(arg.indexOf("=")+1);
                if (key.trim().equals(name)) {
                    System.out.println(key + "=" + value);
                    return value.trim();
                }
            }
        }
        return defaultValue;
    }

    private static int getParameterInt(String name, String[] args, int defaultValue) {
        String valueStr = getParameter(name, args, null);
        if (valueStr == null) {
            return defaultValue;
        } else {
            return Integer.parseInt(valueStr);
        }
    }

    public static void main(String[] args) throws Exception {
        File outputPath = new File("./output");
        if (args.length == 0 || existSetting("h", args) || existSetting("?", args) || existSetting("help", args)) {
            System.out.println("\n---------------------------------------------------------------------------");
            System.out.println("-- Open World Simulator usage:");
            System.out.println("---------------------------------------------------------------------------");
            System.out.println("  - (Help):                     ./run-runSimulation.sh --help");
            System.out.println("  - (Run single experiment):    ./run-runSimulation.sh [months=<num_months>] experiment=<experiment id>");
            System.out.println("  - (Run suite of experiments): ./run-runSimulation.sh [months=<num_months>] suite=<suite id>");
            System.out.println("\nExamples:\n");
            System.out.println(" ./run-runSimulation.sh months=1200 experiment=default");
            System.out.println(" ./run-runSimulation.sh suite=demographics\n");
            System.exit(0);
        }

        int nMonths = getParameterInt("months", args, DEFAULT_MONTHS);
        String experiment = getParameter("experiment", args, DEFAULT_EXPERIMENT);
        String suite = getParameter("suite", args, null);

        Experiments experiments = new Experiments();
        if (suite == null) {
            experiments.runExperiment(outputPath, experiment, nMonths);
        } else {
            experiments.runSuite(outputPath, suite, nMonths);
        }
    }
}
