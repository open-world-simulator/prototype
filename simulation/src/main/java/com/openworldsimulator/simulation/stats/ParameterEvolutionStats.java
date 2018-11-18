package com.openworldsimulator.simulation.stats;

import com.openworldsimulator.simulation.ModelParametersEvolution;
import com.openworldsimulator.simulation.ModelStats;
import com.openworldsimulator.simulation.Simulation;
import com.openworldsimulator.tools.charts.TimeSeriesChartTools;

import java.io.IOException;
import java.util.Arrays;

public class ParameterEvolutionStats extends ModelStats {
    private ModelParametersEvolution modelParametersEvolution;

    public ParameterEvolutionStats(Simulation simulation) {
        super(simulation);
        this.modelParametersEvolution = simulation.getModelParametersEvolution();
    }

    @Override
    protected String getStatsId() {
        return "parameter-evolution";
    }

    @Override
    public void collect(int month) {
        beginMonthStats();
        modelParametersEvolution.getEvolvingParameters().forEach(
                (k) -> {
                    double value = modelParametersEvolution.getLastChangedValue(k);
                    collectMonthStats(k, value);
                }
        );
        endMonthStats();
    }

    @Override
    public void writeChartsAtEnd() {
        if (modelParametersEvolution.getEvolvingParameters().isEmpty()) return;

        try {
            // Write CSV
            modelParametersEvolution.getEvolvingParameters().forEach(
                    (k) -> {
                        try {
                            TimeSeriesChartTools.writeTimeSeriesChart(
                                    getStatsBasePath().getPath(), k,
                                    getChartTitle(k),
                                    "Value",
                                    Arrays.asList(k),
                                    Arrays.asList(buildSumSeries(k))
                                    ,
                                    getSimulation().getBaseYear()
                            );
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
            );

            writeAllAggregatedTimeSeriesCSV("parameters-evolution.csv", getSimulation().getBaseYear());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}