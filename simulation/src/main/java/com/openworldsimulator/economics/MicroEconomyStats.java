package com.openworldsimulator.economics;

import com.openworldsimulator.simulation.ModelStats;
import com.openworldsimulator.simulation.Simulation;

import java.io.IOException;
import java.util.Arrays;

import static com.openworldsimulator.tools.HistogramChartTools.writeHistoChart;
import static com.openworldsimulator.tools.TimeSeriesChartTools.writeTimeSeriesChart;

public class MicroEconomyStats extends ModelStats {

    private static final String INCOME_WAGE = "monthIncomeWage";
    private static final String INCOME_PENSION = "monthIncomePension";
    private static final String INCOME_FINANCIAL = "monthIncomeSavings";
    private static final String INCOME_TOTAL = "incomeTotal";
    private static final String ASSETS_SAVINGS = "assetsSavings";
    private static final String EXPENSES_TOTAL = "expensesTotal";
    private static final String EXPENSES_DISCRETIONARY = "monthExpensesDiscretionary";
    private static final String EXPENSES_NON_DISCRETIONARY = "monthExpensesNonDiscretionary";
    private static final String TAXES_TOTAL = "taxesTotal";
    private static final String NET_SAVINGS = "netSavings";

    public MicroEconomyStats(EconomyParams params, Simulation simulation) {
        super(simulation);
    }

    @Override
    protected String getStatsId() {
        return "economy";
    }


    public void collect(int month) {

        beginMonthStats();

        // Collect monthly stats over all population
        collectMonthStats(INCOME_WAGE, true, p -> p.monthlyData.monthIncomeWage);
        collectMonthStats(INCOME_PENSION, true, p -> p.monthlyData.monthIncomePension);
        collectMonthStats(INCOME_FINANCIAL, true, p -> p.monthlyData.monthIncomeSavings);

        collectMonthStats(INCOME_TOTAL, true, p -> p.monthlyData.getTotalMonthIncome());
        collectMonthStats(ASSETS_SAVINGS, true, p -> p.getBalanceSheet().getSavings());

        collectMonthStats(EXPENSES_TOTAL, true, p -> p.monthlyData.getTotalExpenses() + p.monthlyData.getTotalTaxes());
        collectMonthStats(EXPENSES_DISCRETIONARY, true, p -> p.monthlyData.monthExpensesDiscretionary);
        collectMonthStats(EXPENSES_NON_DISCRETIONARY, true, p -> p.monthlyData.monthExpensesNonDiscretionary);
        collectMonthStats(TAXES_TOTAL, true, p -> p.monthlyData.monthTaxesIncome + p.monthlyData.monthTaxesFinancial + p.monthlyData.monthTaxesConsumption);

        collectMonthStats(NET_SAVINGS, true, p -> p.monthlyData.getTotalMonthNetResult());

        endMonthStats();
    }


    @Override
    public void writeChartsAtEnd() {

        try {
            writeTimeSeriesChart(
                    getStatsBasePath().getPath(), "avg-net-savings",
                    getChartTitle("Average of net savings"),
                    "Average of net savings",
                    Arrays.asList("Total Net Savings", "Income", "Expenses"),
                    Arrays.asList(
                            buildAvgSeries(NET_SAVINGS),
                            buildAvgSeries(INCOME_TOTAL),
                            buildAvgSeries(EXPENSES_TOTAL)
                    )
                    ,
                    getSimulation().getBaseYear()
            );


            writeTimeSeriesChart(
                    getStatsBasePath().getPath(), "sum-net-savings",
                    getChartTitle("Sum of net savings"),
                    "Sum of net savings",
                    Arrays.asList("Total Net Savings", "Income", "Expenses"),
                    Arrays.asList(
                            buildSumSeries(NET_SAVINGS),
                            buildSumSeries(INCOME_TOTAL),
                            buildSumSeries(EXPENSES_TOTAL)
                    )
                    ,
                    getSimulation().getBaseYear()
            );


            writeTimeSeriesChart(
                    getStatsBasePath().getPath(), "avg-individual-income",
                    getChartTitle("Average of monthly income"),
                    "Average of monthly income",
                    Arrays.asList("Total", "Wages", "Pensions", "Financial"),
                    Arrays.asList(
                            buildAvgSeries(INCOME_TOTAL),
                            buildAvgSeries(INCOME_WAGE),
                            buildAvgSeries(INCOME_PENSION),
                            buildAvgSeries(INCOME_FINANCIAL)
                    )
                    ,
                    getSimulation().getBaseYear()
            );

            writeTimeSeriesChart(
                    getStatsBasePath().getPath(), "sum-individual-income",
                    getChartTitle("Sum of monthly income"),
                    "Sum of monthly income",
                    Arrays.asList("Total", "Wages", "Pensions", "Financial"),
                    Arrays.asList(
                            buildSumSeries(INCOME_TOTAL),
                            buildSumSeries(INCOME_WAGE),
                            buildSumSeries(INCOME_PENSION),
                            buildSumSeries(INCOME_FINANCIAL)
                    )
                    ,
                    getSimulation().getBaseYear()
            );

            writeTimeSeriesChart(
                    getStatsBasePath().getPath(), "avg-individual-expenses",
                    getChartTitle("Average of monthly expenses"),
                    "Average of monthly expenses",
                    Arrays.asList("Total", "Discretionary", "Non discretionary", "Taxes"),
                    Arrays.asList(
                            buildAvgSeries(EXPENSES_TOTAL),
                            buildAvgSeries(EXPENSES_DISCRETIONARY),
                            buildAvgSeries(EXPENSES_NON_DISCRETIONARY),
                            buildAvgSeries(TAXES_TOTAL)
                    )
                    ,
                    getSimulation().getBaseYear()
            );

            writeTimeSeriesChart(
                    getStatsBasePath().getPath(), "sum-individual-expenses",
                    "Sum of monthly expenses",
                    getChartTitle("Sum of monthly expenses"),
                    Arrays.asList("Total", "Discretionary", "Non discretionary", "Taxes"),
                    Arrays.asList(
                            buildSumSeries(EXPENSES_TOTAL),
                            buildSumSeries(EXPENSES_DISCRETIONARY),
                            buildSumSeries(EXPENSES_NON_DISCRETIONARY),
                            buildSumSeries(TAXES_TOTAL)
                    )
                    ,
                    getSimulation().getBaseYear()
            );

            writeTimeSeriesChart(
                    getStatsBasePath().getPath(), "avg-individual-assets",
                    getChartTitle("Average of individual assets"),
                    "Average of individual assets",
                    Arrays.asList("Savings"),
                    Arrays.asList(
                            buildAvgSeries(ASSETS_SAVINGS)
                    )
                    ,
                    getSimulation().getBaseYear()
            );

            writeTimeSeriesChart(
                    getStatsBasePath().getPath(), "sum-individual-assets",
                    getChartTitle("Sum of individual assets"),
                    "Sum of individual assets",
                    Arrays.asList("Savings"),
                    Arrays.asList(
                            buildSumSeries(ASSETS_SAVINGS)
                    )
                    ,
                    getSimulation().getBaseYear()
            );

            // Salary distribution
            writeHistoChart(getStatsBasePath().getPath(),
                    "dist-grossMonthlySalary-income",
                    "Salary distribution",
                    histogram(p -> p.isAlive() && p.monthlyData.monthIncomeWage > 0, p -> (long) (p.monthlyData.monthIncomeWage / 100)));

            writeHistoChart(getStatsBasePath().getPath(),
                    "dist-total-income",
                    "Total income distribution",
                    histogram(p -> p.isAlive() && p.monthlyData.getTotalMonthIncome() > 0, p -> (long) (p.monthlyData.getTotalMonthIncome() / 100)));

            // TODO: Unemployment, activity rate,

            // TODO: Average wage, pension, subsidy
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
