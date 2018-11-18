package com.openworldsimulator.simulation;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class ModelParametersEvolution {

    private static final String EVOLVE_YEAR_PCT = "EVOLVE_YEAR_PCT_";

    private Map<String, Double> yearlyLinearChangeRate = new HashMap<>();
    private Map<String, Double> lastValue = new HashMap<>();

    public ModelParametersEvolution() {
    }

    public void loadParametersChangeRate(Properties properties, Map optionalProperties) {
        if (properties != null) {
            load(properties);
        }
        if (optionalProperties != null) {
            load(optionalProperties);
        }
    }

    private void load(Map properties) {
        properties.keySet().forEach(
                k -> {
                    String key = k.toString().trim();
                    if (key.startsWith(EVOLVE_YEAR_PCT)) {
                        String param = key.substring(EVOLVE_YEAR_PCT.length());
                        double rate = Double.valueOf(properties.get(k).toString().trim());
                        yearlyLinearChangeRate.put(param, rate);
                    }
                }
        );
    }

    private double getChangeRate(String property) {
        if (yearlyLinearChangeRate.get(property) == null) {
            return 0.0D;
        } else {
            return yearlyLinearChangeRate.get(property);
        }
    }

    public void evolveMonthly(ModelParameters parameters) {
        yearlyLinearChangeRate.forEach((k, v) -> {
            double changeRate = getChangeRate(k);
            if (changeRate != 0.0D) {
                Double currentValue = parameters.getParameterValueDouble(k);
                if (currentValue != null) {
                    double delta = currentValue * changeRate / 12.0D;
                    double newValue = currentValue + delta;
                    System.out.println("Increasing parameter " + k + " by " + delta);
                    parameters.setParameterValue(k, newValue);
                    // Store value for stats
                    lastValue.put(k, parameters.getParameterValueDouble(k));
                }
            }
        });
    }

    public Set<String> getEvolvingParameters() {
        return yearlyLinearChangeRate.keySet();
    }

    public double getLastChangedValue(String parameter) {
        Double d  = lastValue.get(parameter);
        if( d == null ) {
            System.out.println("[ERROR] Last value for " + parameter + " not found");
            return 0;
        } else {
            return d;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("Yearly parameters evolution:\n");
        yearlyLinearChangeRate.forEach(
                (k, v) -> {
                    builder.append("  - " + k)
                            .append(" = ")
                            .append(String.format("%.02f", v))
                            .append(" => ")
                            .append(String.format("%.02f", v * 100.0))
                            .append("%\n");

                }
        );
        return builder.toString();
    }
}
