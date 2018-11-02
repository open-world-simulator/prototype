package com.openworldsimulator.economics.stats;

import com.openworldsimulator.simulation.ModelStats;
import com.openworldsimulator.simulation.Simulation;
import com.openworldsimulator.tools.charts.TimeSeriesChartTools;

import java.io.IOException;
import java.util.Arrays;

public class MacroBalanceSheetStats extends ModelStats {

    private static final String ASSETS_POPULATION = "assetsPopulation";
    private static final String DEBT_POPULATION = "debtPopulation";

    private static final String ASSETS_COMPANIES = "assetsCompanies";
    private static final String DEBT_COMPANIES = "debtCompanies";

    private static final String ASSETS_PUBLIC_SECTOR = "assetsPublicSector";
    private static final String DEBT_PUBLIC_SECTOR = "debtPublicSector";

    private static final String ASSETS_BANKS = "assetsBanks";
    private static final String DEBT_BANKS = "debtBanks";

    public MacroBalanceSheetStats(Simulation simulation) {
        super(simulation);
    }

    @Override
    protected String getStatsId() {
        return "balance-sheet";
    }


    public void collect(int month) {

        beginMonthStats();

        collectMonthStats(DEBT_PUBLIC_SECTOR, getSimulation().getPublicSector().getBalanceSheet().getDebt());
        collectMonthStats(DEBT_COMPANIES, getSimulation().getCompanies().getBalanceSheet().getDebt());
        collectMonthStats(DEBT_BANKS, getSimulation().getBanks().getBalanceSheet().getSavings());
        collectMonthStats(DEBT_POPULATION, true, p -> p.getBalanceSheet().getDebt());


        collectMonthStats(ASSETS_PUBLIC_SECTOR, getSimulation().getPublicSector().getBalanceSheet().getSavings());
        collectMonthStats(ASSETS_COMPANIES, getSimulation().getCompanies().getBalanceSheet().getSavings());
        collectMonthStats(ASSETS_BANKS, getSimulation().getBanks().getBalanceSheet().getDebt());
        collectMonthStats(ASSETS_POPULATION, true, p -> p.getBalanceSheet().getSavings());

        endMonthStats();
    }

    @Override
    public void writeChartsAtEnd() {

        try {
            TimeSeriesChartTools.writeTimeSeriesChart(
                    getStatsBasePath().getPath(), "total-sectors-assets",
                    getChartTitle("Total assets by sector"),
                    "Assets",
                    Arrays.asList("Public Sector", "Companies", "Banks", "Population"),
                    Arrays.asList(
                            buildAvgSeries(ASSETS_PUBLIC_SECTOR),
                            buildAvgSeries(ASSETS_COMPANIES),
                            buildAvgSeries(ASSETS_BANKS),
                            buildAvgSeries(ASSETS_POPULATION)
                    ),
                    getSimulation().getBaseYear()
            );

            TimeSeriesChartTools.writeTimeSeriesChart(
                    getStatsBasePath().getPath(), "total-sectors-debt",
                    getChartTitle("Total debt by sector"),
                    "Debt by sector",
                    Arrays.asList("Public Sector", "Companies", "Banks", "Population"),
                    Arrays.asList(
                            buildAvgSeries(DEBT_PUBLIC_SECTOR),
                            buildAvgSeries(DEBT_COMPANIES),
                            buildAvgSeries(DEBT_BANKS),
                            buildAvgSeries(DEBT_POPULATION)
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
