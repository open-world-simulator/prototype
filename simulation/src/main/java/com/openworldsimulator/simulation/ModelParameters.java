package com.openworldsimulator.simulation;

import com.openworldsimulator.tools.ModelParametersTools;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

public abstract class ModelParameters {

    public List<String> getParameterNames() {
        return ModelParametersTools.getParameterNames(this);
    }

    public Double getParameterValueDouble(String fieldName) {
        return ModelParametersTools.getParameterValueDouble(this, fieldName);
    }

    public String getParameterValueString(String fieldName) {
        return ModelParametersTools.getParameterValueString(this, fieldName);
    }

    public void loadParameterValues(Properties defaultProperties, Map overrideProperties) {
        ModelParametersTools.updateParameterValues(defaultProperties, overrideProperties, this);
    }

    public void setParameterValue(String fieldName, double fieldValue) {
        ModelParametersTools.setParameterValue(fieldName, fieldValue, this);
    }

 /*
    public Map<String, Double> getParameterMapForDouble() {
        Map<String, Double> params = new TreeMap<>();
        getParameterNames().forEach(p -> {
                    if (ModelParametersTools.isDouble(this, p)) {
                        Double d = getParameterValueDouble(p);
                        if (d != null) {
                            params.put(p, d);
                        }
                    }
                }
        );
        return params;
    }
*/
    public String toString() {
        return ModelParametersTools.toString(this);
    }
}
