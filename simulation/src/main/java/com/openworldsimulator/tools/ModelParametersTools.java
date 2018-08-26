package com.openworldsimulator.tools;

import com.openworldsimulator.simulation.ModelParameters;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ModelParametersTools {
    /**
     * Overrides public attributes in a configuration object according to the values supplied in a Properties object and
     * and optional additional Properties object
     */
    public static <T extends ModelParameters> T loadParameterValues(Properties defaultProperties, Map overrideProperties, T params) {

        params = loadParameterValues(defaultProperties, params);
        params = loadParameterValues(overrideProperties, params);
        return params;
    }

    private static <T extends ModelParameters> T loadParameterValues(Map properties, T params) {
        if (properties == null) {
            return params;
        }

        properties.keySet().forEach(
                k -> {
                    String key = k.toString().trim();
                    if (!key.startsWith("#")) { // IS PARAMETER
                        try {
                            double value = Double.valueOf(properties.get(k).toString().trim());
                            //System.out.println("Setting property '" + key + "' to '" + value + "");
                            setParameterValue(params, key, value);
                        } catch (Exception e) {
                            //System.out.println("[ERROR] Failed to set property '" + key + "' with value '" + properties.get(k) + "' on " + params.getClass().getName());
                        }
                    }
                }
        );


        return params;
    }

    public static void loadParameterChanges(Properties properties, Map<String, Double> paramChanges) {
        if (properties == null) {
            return;
        }

        properties.keySet().forEach(
                k -> {
                    String key = k.toString().trim();
                    if (key.startsWith("CHANGE_")) {
                        String param = key.substring("CHANGE_".length());
                        double rate = Double.valueOf(properties.get(k).toString().trim());
                        paramChanges.put(param, rate);
                    }
                }
        );
    }

    private static void setParameterValue(ModelParameters modelParameters, String fieldName, double fieldValue) {
        Class<?> clazz = modelParameters.getClass();
        try {
            Field field = clazz.getDeclaredField(fieldName);
            if (field != null) {
                field.setAccessible(true);
                if (field.getType().equals(double.class)) {
                    field.setDouble(modelParameters, fieldValue);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns the list of parameters
     *
     * @param modelParameters
     * @return
     */
    public static List<String> getParameterNames(ModelParameters modelParameters) {
        Class<?> clazz = modelParameters.getClass();
        List<String> paramNames = new ArrayList<>();

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            paramNames.add(field.getName());
        }

        paramNames.sort(String::compareTo);
        return paramNames;
    }

    /**
     * Returns parameter value by
     *
     * @param modelParameters
     * @param fieldName
     * @return
     */
    public static Double getParameterValueDouble(ModelParameters modelParameters, String fieldName) {
        Class<?> clazz = modelParameters.getClass();
        try {
            Field field = clazz.getDeclaredField(fieldName);
            if (field != null && field.getType().isAssignableFrom(double.class)) {
                return field.getDouble(modelParameters);
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        return null;
    }

    public static String getParameterValueString(ModelParameters modelParameters, String fieldName) {
        Class<?> clazz = modelParameters.getClass();
        try {
            Field field = clazz.getDeclaredField(fieldName);
            if (field != null && field.getType().isAssignableFrom(String.class)) {
                if (field.get(modelParameters) == null) {
                    return null;
                } else {
                    return String.valueOf(field.get(modelParameters));
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        return null;
    }

    /**
     * Implements generic toString for model parameters
     *
     * @param parameters
     * @return
     */
    public static String toString(ModelParameters parameters) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Parameters: ")
                .append(parameters.getClass().getSimpleName())
                .append("\n");

        getParameterNames(parameters).forEach(
                s -> {
                    Double value = getParameterValueDouble(parameters, s);
                    if (value != null) {
                        buffer.append("  - ")
                                .append(s)
                                .append(" = ")
                                .append(String.format("%.02f", value));

                        if (s.endsWith("_RATE")) {
                            buffer.append(" (").append(String.format("%.01f", value * 100.0)).append("%)");
                        }
                        buffer.append("\n");
                    }
                }
        );

        return buffer.toString();
    }

    public static void evolveParameterDeltaMonthly(ModelParameters parameters, Map<String, Double> parametersChangeAnualRate) {
        for (String p : getParameterNames(parameters)) {
            Double changeRate = parametersChangeAnualRate.get(p);
            if (changeRate != null && changeRate == 0.0D) {
                double currentValue = getParameterValueDouble(parameters, p);
                double delta = currentValue * changeRate / 12.0D;
                double newValue = currentValue + delta;
                System.out.println("Increasing parameter " + p + " by " + delta);
                setParameterValue(parameters, p, newValue);
            }
        }
    }
}