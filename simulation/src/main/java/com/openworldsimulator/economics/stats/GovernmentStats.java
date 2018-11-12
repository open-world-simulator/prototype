package com.openworldsimulator.economics.stats;

import com.openworldsimulator.model.Government;
import com.openworldsimulator.model.MonthlyResults;
import com.openworldsimulator.simulation.ModelStats;
import com.openworldsimulator.simulation.Simulation;
import com.openworldsimulator.tools.charts.TimeSeriesChartTools;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class GovernmentStats extends ModelStats {

    private static final String ASSETS = "government_assets";
    private static final String DEBT = "government_debt";

    private static final String EXPENSES_TOTAL = "government_expenses";
    private static final String EXPENSES_PENSIONS = "government_expenses_pensions";
    private static final String EXPENSES_PUBLIC_EMPLOYMENT = "government_expenses_public_employment";
    private static final String EXPENSES_OTHER = "government_general_expenses";
    private static final String EXPENSES_DEBT_INTERESTS = "government_expenses_debt_interests";

    private static final String INCOME_TOTAL = "government_income";
    private static final String INCOME_TAX_INCOME = "government_tax_income";
    private static final String INCOME_TAX_SAVINGS = "government_tax_savings";
    private static final String INCOME_TAX_BUSINESS = "government_tax_business";
    private static final String INCOME_TAX_CONSUMPTION = "government_tax_consumption";

    private static final String DEFICIT = "government_deficit";

    public GovernmentStats(Simulation simulation) {
        super(simulation);
    }

    @Override
    protected String getStatsId() {
        return "government";
    }


    public void collect(int month) {

        beginMonthStats();

        Government government = getSimulation().getGovernment();
        MonthlyResults results = government.getMonthlyResults();

        collectMonthStats(DEBT, government.getBalanceSheet().getDebt());
        collectMonthStats(ASSETS, government.getBalanceSheet().getSavings());

        collectMonthStats(EXPENSES_TOTAL, results.getMonthlyExpenses());
        collectMonthStats(EXPENSES_DEBT_INTERESTS, results.getMonthlyExpenses(Government.TYPE_EXPENSES_DEBT));
        collectMonthStats(EXPENSES_PENSIONS, results.getMonthlyExpenses(Government.TYPE_EXPENSES_PENSIONS));
        collectMonthStats(EXPENSES_PUBLIC_EMPLOYMENT, results.getMonthlyExpenses(Government.TYPE_EXPENSES_EMPLOYMENT));
        collectMonthStats(EXPENSES_OTHER, results.getMonthlyExpenses(Government.TYPE_EXPENSES_OTHER));

        collectMonthStats(INCOME_TOTAL, results.getMonthlyIncome());
        collectMonthStats(INCOME_TAX_INCOME, results.getMonthlyIncome(Government.TYPE_INCOME_TAXES_INCOME));
        collectMonthStats(INCOME_TAX_SAVINGS, results.getMonthlyIncome(Government.TYPE_INCOME_TAXES_SAVINGS));
        collectMonthStats(INCOME_TAX_BUSINESS, results.getMonthlyIncome(Government.TYPE_INCOME_TAXES_BUSINESS));
        collectMonthStats(INCOME_TAX_CONSUMPTION, results.getMonthlyIncome(Government.TYPE_INCOME_TAXES_CONSUMPTION));

        collectMonthStats(DEFICIT, results.getMonthlyResult());

        endMonthStats();
    }

    @Override
    public void writeChartsAtEnd() {

        try {
            // To convert to real magnitudes
            double populationScalingFactor = getPopulation().getSegmentRepresentationRatio();

            File incomePath = getStatsDir("income");
            File expensesPath = getStatsDir("expenses");

            TimeSeriesChartTools.writeTimeSeriesChart(
                    getStatsBasePath().getPath(), "government-flows",
                    getChartTitle("Government income and expending"),
                    "Amount",
                    Arrays.asList("Income", "Expenses", "Deficit"),
                    Arrays.asList(
                            buildAvgSeries(INCOME_TOTAL),
                            buildAvgSeries(EXPENSES_TOTAL),
                            buildAvgSeries(DEFICIT)
                    )
                    ,
                    getSimulation().getBaseYear()
            );

            TimeSeriesChartTools.writeTimeSeriesChart(
                    getStatsBasePath().getPath(), "government-expending",
                    getChartTitle("Government expending by type"),
                    "Amount",
                    Arrays.asList("Total", "Public employment", "Pensions", "Debt service", "Other"),
                    Arrays.asList(
                            buildAvgSeries(EXPENSES_TOTAL),
                            buildAvgSeries(EXPENSES_PUBLIC_EMPLOYMENT),
                            buildAvgSeries(EXPENSES_PENSIONS),
                            buildAvgSeries(EXPENSES_DEBT_INTERESTS),
                            buildAvgSeries(EXPENSES_OTHER)
                    )
                    ,
                    getSimulation().getBaseYear()
            );

            TimeSeriesChartTools.writeTimeSeriesChart(
                    getStatsBasePath().getPath(), "government-income",
                    getChartTitle("Government income by type"),
                    "Amount",
                    Arrays.asList("Total", "Tax on income", "Tax on savings", "Tax on business", "Tax on consumption"),
                    Arrays.asList(
                            buildAvgSeries(INCOME_TOTAL),
                            buildAvgSeries(INCOME_TAX_INCOME),
                            buildAvgSeries(INCOME_TAX_SAVINGS),
                            buildAvgSeries(INCOME_TAX_BUSINESS),
                            buildAvgSeries(INCOME_TAX_CONSUMPTION)
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
