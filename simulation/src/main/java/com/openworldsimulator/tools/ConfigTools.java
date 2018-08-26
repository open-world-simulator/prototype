package com.openworldsimulator.tools;

public class ConfigTools {
    public static String getConfig(String variable, String defaultValue) {
        System.out.println("Checking for system property " + variable);
        String value = System.getProperty(variable);
        if (value == null) {
            System.out.println("Checking for env var " + variable);
            value = System.getenv(variable);
            if (value == null) {
                value = defaultValue;
            }
        }

        return value;
    }
}
