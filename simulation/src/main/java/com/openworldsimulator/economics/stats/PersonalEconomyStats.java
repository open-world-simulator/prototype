package com.openworldsimulator.economics.stats;

import com.openworldsimulator.economics.EconomyParams;
import com.openworldsimulator.simulation.ModelStats;
import com.openworldsimulator.simulation.Simulation;

import java.io.IOException;
import java.util.Arrays;

import static com.openworldsimulator.tools.charts.HistogramChartTools.writeHistoChart;
import static com.openworldsimulator.tools.charts.TimeSeriesChartTools.writeTimeSeriesChart;

public class PersonalEconomyStats extends ModelStats {

    private static final String INCOME_WAGE = "personalIncomeWages";
    private static final String INCOME_PENSION = "personalIncomePension";
    private static final String INCOME_FINANCIAL = "personalIncomeSavings";
    private static final String INCOME_TOTAL = "personalIncomeTotal";
    private static final String EXPENSES_TOTAL = "personalExpensesTotal";
    private static final String EXPENSES_DISCRETIONARY = "personalExpensesDiscretionary";
    private static final String EXPENSES_NON_DISCRETIONARY = "personalExpensesNonDiscretionary";

    private static final String NET_SAVINGS = "personalNetSavings";

    // TODO: Track types of taxation
    // TODO: Assets / debt

    public PersonalEconomyStats(Simulation simulation) {
        super(simulation);
    }

    @Override
    protected String getStatsId() {
        return "personal";
    }


    public void collect(int month) {

        beginMonthStats();

        // Collect monthly stats over all population
        collectMonthStats(INCOME_WAGE, true, p -> p.monthlyData.monthIncomeWage);
        collectMonthStats(INCOME_PENSION, true, p -> p.monthlyData.monthIncomePension);
        collectMonthStats(INCOME_FINANCIAL, true, p -> p.monthlyData.monthIncomeSavings);

        collectMonthStats(INCOME_TOTAL, true, p -> p.monthlyData.getTotalMonthIncome());
//      collectMonthStats(ASSETS_SAVINGS, true, p -> p.getBalanceSheet().getSavings());

        collectMonthStats(EXPENSES_TOTAL, true, p -> p.monthlyData.getTotalExpenses() + p.monthlyData.getTotalTaxes());
        collectMonthStats(EXPENSES_DISCRETIONARY, true, p -> p.monthlyData.monthExpensesDiscretionary);
        collectMonthStats(EXPENSES_NON_DISCRETIONARY, true, p -> p.monthlyData.monthExpensesNonDiscretionary);
        //collectMonthStats(EXPENSES_TAXES_TOTAL, true, p -> p.monthlyData.monthTaxesIncome + p.monthlyData.monthTaxesFinancial + p.monthlyData.monthTaxesConsumption);

        collectMonthStats(NET_SAVINGS, true, p -> p.monthlyData.getTotalMonthNetResult());

        endMonthStats();
    }


    @Override
    public void writeChartsAtEnd() {

        try {
            writeTimeSeriesChart(
                    getStatsBasePath().getPath(), "personal-avg-net-savings",
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
                    getStatsBasePath().getPath(), "personal-income",
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
                    getStatsBasePath().getPath(), "total-monthly-income",
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
                    getStatsBasePath().getPath(), "personal-expenses",
                    getChartTitle("Average of monthly expenses"),
                    "Average of monthly expenses",
                    Arrays.asList("Total", "Discretionary", "Non discretionary"),
                    Arrays.asList(
                            buildAvgSeries(EXPENSES_TOTAL),
                            buildAvgSeries(EXPENSES_DISCRETIONARY),
                            buildAvgSeries(EXPENSES_NON_DISCRETIONARY)
                    )
                    ,
                    getSimulation().getBaseYear()
            );

            writeTimeSeriesChart(
                    getStatsBasePath().getPath(), "sum-personal-expenses",
                    "Sum of monthly expenses",
                    getChartTitle("Sum of monthly expenses"),
                    Arrays.asList("Total", "Discretionary", "Non discretionary"),
                    Arrays.asList(
                            buildSumSeries(EXPENSES_TOTAL),
                            buildSumSeries(EXPENSES_DISCRETIONARY),
                            buildSumSeries(EXPENSES_NON_DISCRETIONARY)
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

            // Write CSV
            writeAllAggregatedTimeSeriesCSV("series.csv", getSimulation().getBaseYear());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
