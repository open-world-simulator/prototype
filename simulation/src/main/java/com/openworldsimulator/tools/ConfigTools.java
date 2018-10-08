package com.openworldsimulator.tools;

public class ConfigTools {
    public static String getConfig(String variable, String defaultValue) {
        System.out.println("! Checking for system property " + variable + " - default: " + defaultValue);
        String value = System.getProperty(variable);
        if (value == null) {
            value = System.getenv(variable);
            if (value == null) {
                value = defaultValue;
            }
        }

        return value;
    }

    public static int getConfigInt(String variable, int defaultValue) {
        return Integer.parseInt(getConfig(variable, String.valueOf(defaultValue)));
    }
}
