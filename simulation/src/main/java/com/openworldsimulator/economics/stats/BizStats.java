package com.openworldsimulator.economics.stats;

import com.openworldsimulator.simulation.ModelStats;
import com.openworldsimulator.simulation.Simulation;
import com.openworldsimulator.tools.charts.TimeSeriesChartTools;

import java.io.IOException;
import java.util.Arrays;

public class BizStats extends ModelStats {

    private static final String BUSINESS_ASSETS = "businessAssets";
    private static final String BUSINESS_DEBT = "businessDebt";
    private static final String BUSINESS_NET_INCOME = "businessNetIncome";


    public BizStats(Simulation simulation) {
        super(simulation);
    }

    @Override
    protected String getStatsId() {
        return "business";
    }


    public void collect(int month) {

        beginMonthStats();

        collectMonthStats(BUSINESS_DEBT, getSimulation().getCompanies().getBalanceSheet().getDebt());
        collectMonthStats(BUSINESS_ASSETS, getSimulation().getCompanies().getBalanceSheet().getSavings());
        collectMonthStats(BUSINESS_NET_INCOME, getSimulation().getCompanies().getMonthlyResults().getMonthlyResult());

        endMonthStats();
    }

    @Override
    public void writeChartsAtEnd() {

        try {
            TimeSeriesChartTools.writeTimeSeriesChart(
                    getStatsBasePath().getPath(), "business-balance-sheet",
                    getChartTitle("Business assets and liabilities"),
                    "Assets",
                    Arrays.asList("Assets", "Debt"),
                    Arrays.asList(
                            buildAvgSeries(BUSINESS_ASSETS),
                            buildAvgSeries(BUSINESS_DEBT)
                    ),
                    getSimulation().getBaseYear()
            );

            TimeSeriesChartTools.writeTimeSeriesChart(
                    getStatsBasePath().getPath(), "business-results",
                    getChartTitle("Business results"),
                    "Amount",
                    Arrays.asList("Net income"),
                    Arrays.asList(
                            buildAvgSeries(BUSINESS_NET_INCOME)
                    ),
                    getSimulation().getBaseYear()
            );

            // Write CSV
            writeAllAggregatedTimeSeriesCSV("series.csv", getSimulation().getBaseYear());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
