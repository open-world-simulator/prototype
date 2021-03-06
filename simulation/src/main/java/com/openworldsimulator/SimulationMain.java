package com.openworldsimulator;

import com.openworldsimulator.experiments.Experiment;
import com.openworldsimulator.experiments.ExperimentsManager;

import java.io.File;
import java.util.*;

public class SimulationMain {

    private static Set<String> settings = new HashSet<>();
    private static Map<String, String> optionalParams = new HashMap<>();
    private static List<String> baseParams = new ArrayList<>();

    private static int getParameterInt(String name, int defaultValue) {
        String valueStr = optionalParams.get(name);
        if (valueStr == null) {
            return defaultValue;
        } else {
            return Integer.parseInt(valueStr);
        }
    }

    private static String getParameterStr(String name, String defaultValue) {
        String valueStr = optionalParams.get(name);
        if (valueStr == null) {
            return defaultValue;
        } else {
            return valueStr;
        }
    }

    private static void parseArgs(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("--")) {
                settings.add(arg.substring(2));
            } else if (arg.startsWith("-")) {
                settings.add(arg.substring(1));
            } else if (arg.contains("=")) {
                String key = arg.substring(0, arg.indexOf("="));
                String value = arg.substring(arg.indexOf("=") + 1);
                optionalParams.put(key, value);
            } else {
                baseParams.add(arg);
            }
        }
    }

    public static void main(String[] args) throws Exception {

        System.out.println(args.length);
        System.out.println(args[0]);
        parseArgs(args);

        if (baseParams.size() < 2 || settings.contains("h") || settings.contains("?") || settings.contains("help")) {
            System.out.println("\n---------------------------------------------------------------------------");
            System.out.println("-- Open World Simulator usage:");
            System.out.println("---------------------------------------------------------------------------");
            System.out.println("  - ./ows.sh --help");
            System.out.println("  - ./ows.sh <experiment name> <base config> [months=<num_months>] [year=<base_year>] [output=<output dir>] [<SIMULATION_PARAM>=<value>]*");
            System.out.println("\nExamples:\n");
            System.out.println(" ./ows.sh my-sim-1 Narnia.defaults months=1200 year=2016");
            System.out.println(" ./ows.sh my-sim-2 Narnia.defaults months=1200 year=2016 INITIAL_POPULATION_SIZE=500000");
            System.out.println("\n\n");
            System.exit(0);
        }

        String experimentId = baseParams.get(0);
        String baseConfig = baseParams.get(1);
        int nMonths  = getParameterInt("months", ExperimentsManager.DEFAULT_EXPERIMENT_DURATION);
        int baseYear = getParameterInt("year", ExperimentsManager.DEFAULT_EXPERIMENT_YEAR);

        File baseOutputPath = new File(getParameterStr("output", "./output"));
        ExperimentsManager experimentsManager = new ExperimentsManager(baseOutputPath);

        Experiment experiment = new Experiment(
                baseYear,
                nMonths,
                experimentsManager,
                experimentId,
                baseConfig,
                optionalParams
        );

        experiment.run();
    }
}
