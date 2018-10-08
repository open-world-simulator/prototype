package com.openworldsimulator.experiments;

public class ExperimentResult {
    private String path;

    public ExperimentResult(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "ExperimentResult{" +
                "path='" + path + '\'' +
                '}';
    }
}
