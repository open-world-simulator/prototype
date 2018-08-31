package com.openworldsimulator.economics;

import com.openworldsimulator.simulation.ModelStats;
import com.openworldsimulator.simulation.Simulation;
import com.openworldsimulator.tools.ChartTools;

import java.util.Arrays;

public class MacroEconomyStats extends ModelStats {

    private static final String ASSETS_POPULATION = "populationAssets";
    private static final String DEBT_POPULATION = "populationDebt";
    private static final String MONTHLY_RESULT_POPULATION = "monthlyResultPopulation";

    private static final String ASSETS_COMPANIES = "companiesAssets";
    private static final String DEBT_COMPANIES = "companiesDebt";
    private static final String MONTHLY_RESULT_COMPANIES = "monthlyResultCompanies";

    private static final String ASSETS_PUBLIC_SECTOR = "publicSectorAssets";
    private static final String DEBT_PUBLIC_SECTOR = "publicSectorDebt";

    private static final String MONTHLY_PUBLIC_SECTOR_EXPENSES = "monthlyResultPublicSectorExpenses";
    private static final String MONTHLY_PUBLIC_SECTOR_INCOME   = "monthlyResultPublicSectorIncome";
    private static final String MONTHLY_PUBLIC_SECTOR_DEFICIT  = "monthlyResultPublicSectorDeficit";

    private static final String ASSETS_BANKS = "banksAssets";
    private static final String DEBT_BANKS = "banksDebt";
    private static final String MONTHLY_RESULT_BANKS = "monthlyResultBanks";

    public MacroEconomyStats(Simulation simulation) {
        super(simulation);
    }

    @Override
    protected String getStatsId() {
        return "macro";
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

        collectMonthStats(MONTHLY_RESULT_COMPANIES, getSimulation().getCompanies().getMonthlyResults().getMonthlyResult());
        collectMonthStats(MONTHLY_RESULT_BANKS, getSimulation().getBanks().getMonthlyResults().getMonthlyResult());
        collectMonthStats(MONTHLY_RESULT_POPULATION, true, p -> p.monthlyData.getTotalMonthNetResult());

        collectMonthStats(MONTHLY_PUBLIC_SECTOR_EXPENSES, getSimulation().getPublicSector().getMonthlyResults().getMonthlyExpenses());
        collectMonthStats(MONTHLY_PUBLIC_SECTOR_INCOME, getSimulation().getPublicSector().getMonthlyResults().getMonthlyIncome());
        collectMonthStats(MONTHLY_PUBLIC_SECTOR_DEFICIT, getSimulation().getPublicSector().getMonthlyResults().getMonthlyResult());

        endMonthStats();
    }

    @Override
    public void writeChartsAtEnd() {

        ChartTools.writeTimeChart(
                getStatsBasePath().getPath(), "total-sectors-assets", "Total assets by sector",
                Arrays.asList("Public Sector", "Companies", "Banks", "Population"),
                Arrays.asList(
                        buildAvgSeries(ASSETS_PUBLIC_SECTOR),
                        buildAvgSeries(ASSETS_COMPANIES),
                        buildAvgSeries(ASSETS_BANKS),
                        buildAvgSeries(ASSETS_POPULATION)
                ),
                getSimulation().getBaseYear()
        );

        ChartTools.writeTimeChart(
                getStatsBasePath().getPath(), "total-sectors-debt", "Total debt by sector",
                Arrays.asList("Public Sector", "Companies", "Banks", "Population"),
                Arrays.asList(
                        buildAvgSeries(DEBT_PUBLIC_SECTOR),
                        buildAvgSeries(DEBT_COMPANIES),
                        buildAvgSeries(DEBT_BANKS),
                        buildAvgSeries(DEBT_POPULATION)
                ),
                getSimulation().getBaseYear()
        );

        ChartTools.writeTimeChart(
                getStatsBasePath().getPath(), "total-sectors-monthly-results", "Total monthly results",
                Arrays.asList("Companies", "Banks", "Population"),
                Arrays.asList(
                        buildAvgSeries(MONTHLY_RESULT_COMPANIES),
                        buildAvgSeries(MONTHLY_RESULT_BANKS),
                        buildAvgSeries(MONTHLY_RESULT_POPULATION)
                ),
                getSimulation().getBaseYear()
        );

        ChartTools.writeTimeChart(
                getStatsBasePath().getPath(), "public-sector-flows", "Public Sector monthly results",
                Arrays.asList("Income", "Expenses", "Deficit"),
                Arrays.asList(
                        buildAvgSeries(MONTHLY_PUBLIC_SECTOR_INCOME),
                        buildAvgSeries(MONTHLY_PUBLIC_SECTOR_EXPENSES),
                        buildAvgSeries(MONTHLY_PUBLIC_SECTOR_DEFICIT)
                )
                ,
                getSimulation().getBaseYear()
        );
    }
}
