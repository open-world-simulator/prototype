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
    public static void updateParameterValues(Properties defaultProperties, Map overrideProperties, ModelParameters params) {
        updateParameterValues(defaultProperties, params);
        updateParameterValues(overrideProperties, params);
    }

    private static void updateParameterValues(Map properties, ModelParameters params) {
        if (properties == null) {
            return;
        }

        properties.keySet().forEach(
                k -> {
                    String key = k.toString().trim();
                    if (!key.startsWith("#")) { // IS PARAMETER
                        try {
                            if (isDouble(params, key)) {
                                double value = Double.valueOf(properties.get(k).toString().trim());
                                System.out.printf("[%s] Setting property %s -> %.2f\n", params.getClass().getName(), key, value);
                                setParameterValue(key, value, params);
                            } else if (isString(params, key)) {
                                String value = properties.get(k).toString().trim();
                                System.out.printf("[%s] Setting property %s -> %s\n", params.getClass().getName(), key, value);
                                setParameterValue(params, key, value);
                            }
                        } catch (Exception e) {
                            //System.out.println("[ERROR] Failed to set property '" + key + "' with value '" + properties.get(k) + "' on " + params.getClass().getName());
                        }
                    }
                }
        );
    }


    public static void setParameterValue(String fieldName, double fieldValue, ModelParameters modelParameters) {
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

    private static void setParameterValue(ModelParameters modelParameters, String fieldName, String fieldValue) {
        Class<?> clazz = modelParameters.getClass();
        try {
            Field field = clazz.getDeclaredField(fieldName);
            if (field != null) {
                field.setAccessible(true);
                if (field.getType().equals(String.class)) {
                    field.set(modelParameters, fieldValue);
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

    public static boolean isDouble(ModelParameters modelParameters, String fieldName) {
        Class<?> clazz = modelParameters.getClass();
        try {
            Field field = clazz.getDeclaredField(fieldName);
            return (field != null && field.getType().isAssignableFrom(double.class));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static boolean isString(ModelParameters modelParameters, String fieldName) {
        Class<?> clazz = modelParameters.getClass();
        try {
            Field field = clazz.getDeclaredField(fieldName);
            return (field != null && field.getType().isAssignableFrom(String.class));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static Double getParameterValueDouble(ModelParameters modelParameters, String fieldName) {
        Class<?> clazz = modelParameters.getClass();
        try {
            Field field = clazz.getDeclaredField(fieldName);
            if (field != null && field.getType().isAssignableFrom(double.class)) {
                return field.getDouble(modelParameters);
            }
        } catch (Exception e) {
            // Do nothing
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
}