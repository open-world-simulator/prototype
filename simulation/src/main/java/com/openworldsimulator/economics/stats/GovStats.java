package com.openworldsimulator.economics.stats;

import com.openworldsimulator.simulation.ModelStats;
import com.openworldsimulator.simulation.Simulation;
import com.openworldsimulator.tools.charts.TimeSeriesChartTools;

import java.io.IOException;
import java.util.Arrays;

public class GovStats extends ModelStats {

    private static final String GOV_ASSETS   = "govAssets";
    private static final String GOV_DEBT     = "govDebt";

    private static final String GOV_EXPENSES = "govExpenses";
    private static final String GOV_INCOME   = "government";
    private static final String GOV_DEFICIT  = "govDeficit";

    // TODO: Track income by type of taxes (social security, personal income, consumption, ...)
    // TODO: Track expenses by type of expenses (pensions, public expenses, investment,...)

    public GovStats(Simulation simulation) {
        super(simulation);
    }

    @Override
    protected String getStatsId() {
        return "government";
    }


    public void collect(int month) {

        beginMonthStats();

        collectMonthStats(GOV_DEBT, getSimulation().getPublicSector().getBalanceSheet().getDebt());
        collectMonthStats(GOV_ASSETS, getSimulation().getPublicSector().getBalanceSheet().getSavings());

        collectMonthStats(GOV_EXPENSES, getSimulation().getPublicSector().getMonthlyResults().getMonthlyExpenses());
        collectMonthStats(GOV_INCOME, getSimulation().getPublicSector().getMonthlyResults().getMonthlyIncome());
        collectMonthStats(GOV_DEFICIT, getSimulation().getPublicSector().getMonthlyResults().getMonthlyResult());

        endMonthStats();
    }

    @Override
    public void writeChartsAtEnd() {

        try {
            TimeSeriesChartTools.writeTimeSeriesChart(
                    getStatsBasePath().getPath(), "government-flows",
                    getChartTitle("Government income and expending"),
                    "Amount",
                    Arrays.asList("Income", "Expenses", "Deficit"),
                    Arrays.asList(
                            buildAvgSeries(GOV_INCOME),
                            buildAvgSeries(GOV_EXPENSES),
                            buildAvgSeries(GOV_DEFICIT)
                    )
                    ,
                    getSimulation().getBaseYear()
            );

            // Write CSV
            writeAllAggregatedTimeSeriesCSV("series.csv", getSimulation().getBaseYear());
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
