package com.openworldsimulator.simulation;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class ModelParametersEvolution {

    private static final String EVOLVE_TOTAL_PCT = "EVOLVE_TOTAL_PCT_";

    private Map<String, Double> linearChangePct = new HashMap<>();
    private Map<String, Double> initialValue = new HashMap<>();
    private Map<String, Double> lastValue = new HashMap<>();

    private Simulation simulation;

    public ModelParametersEvolution(Simulation simulation) {
        this.simulation = simulation;
    }

    public void loadParametersChangeRate(Properties properties, Map optionalProperties) {
        if (properties != null) {
            load(properties);
        }
        if (optionalProperties != null) {
            load(optionalProperties);
        }
        simulation.log(this.toString());
    }

    private void load(Map properties) {
        properties.keySet().forEach(
                k -> {
                    String key = k.toString().trim();
                    if (key.startsWith(EVOLVE_TOTAL_PCT)) {
                        String param = key.substring(EVOLVE_TOTAL_PCT.length());
                        double rate = Double.valueOf(properties.get(k).toString().trim());
                        linearChangePct.put(param, rate);
                    }
                }
        );
    }

    private double getChangeRate(String property) {
        if (linearChangePct.get(property) == null) {
            return 0.0D;
        } else {
            return linearChangePct.get(property);
        }
    }

    public void evolveMonthly(ModelParameters parameters, int month) {
        linearChangePct.forEach((k, v) -> {
            double changeRate = getChangeRate(k);
            if (changeRate != 0.0D) {
                Double currentValue = parameters.getParameterValueDouble(k);
                if (currentValue != null) {
                    if (initialValue.get(k) == null) {
                        // Save initial value
                        initialValue.put(k, parameters.getParameterValueDouble(k));
                    }

                    double startValue = initialValue.get(k);
                    double endValue = startValue * (1 + changeRate / 100.0D);
                    double delta = (endValue - startValue) / (double) simulation.getTotalMonths();

                    double newValue = startValue + delta * month;

                    simulation.logDebug("Evolving parameter " + k + " to " + newValue + " " + (newValue - startValue) / startValue * 100.0 + "%");
                    parameters.setParameterValue(k, newValue);

                    // Store value for stats
                    lastValue.put(k, newValue);
                }
            }
        });
    }

    public Set<String> getEvolvingParameters() {
        return linearChangePct.keySet();
    }

    public double getLastChangedValue(String parameter) {
        Double d = lastValue.get(parameter);
        if (d == null) {
            System.out.println("[ERROR] Last value for " + parameter + " not found");
            return 0;
        } else {
            return d;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("Parameters change in total percent:\n");
        linearChangePct.forEach(
                (k, v) -> {
                    builder.append("  - " + k)
                            .append(" = ")
                            .append(String.format("%.02f", v))
                            .append("%\n");

                }
        );
        return builder.toString();
    }
}
