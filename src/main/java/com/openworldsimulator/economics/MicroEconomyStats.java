package com.openworldsimulator.economics;

import com.openworldsimulator.simulation.ModelStats;
import com.openworldsimulator.simulation.Simulation;
import com.openworldsimulator.tools.ChartTools;

import java.util.Arrays;

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

        ChartTools.writeTimeChart(
                getStatsBasePath().getPath(), "avg-net-savings", "Average of net savings",
                Arrays.asList("Total Net Savings", "Income", "Expenses"),
                Arrays.asList(
                        buildAvgSeries(NET_SAVINGS),
                        buildAvgSeries(INCOME_TOTAL),
                        buildAvgSeries(EXPENSES_TOTAL)
                )
        );


        ChartTools.writeTimeChart(
                getStatsBasePath().getPath(), "sum-net-savings", "Sum of net savings",
                Arrays.asList("Total Net Savings", "Income", "Expenses"),
                Arrays.asList(
                        buildSumSeries(NET_SAVINGS),
                        buildSumSeries(INCOME_TOTAL),
                        buildSumSeries(EXPENSES_TOTAL)
                )
        );


        ChartTools.writeTimeChart(
                getStatsBasePath().getPath(), "avg-individual-income", "Average of monthly income",
                Arrays.asList("Total", "Wages", "Pensions", "Financial"),
                Arrays.asList(
                        buildAvgSeries(INCOME_TOTAL),
                        buildAvgSeries(INCOME_WAGE),
                        buildAvgSeries(INCOME_PENSION),
                        buildAvgSeries(INCOME_FINANCIAL)
                )
        );

        ChartTools.writeTimeChart(
                getStatsBasePath().getPath(), "sum-individual-income", "Sum of monthly income",
                Arrays.asList("Total", "Wages", "Pensions", "Financial"),
                Arrays.asList(
                        buildSumSeries(INCOME_TOTAL),
                        buildSumSeries(INCOME_WAGE),
                        buildSumSeries(INCOME_PENSION),
                        buildSumSeries(INCOME_FINANCIAL)
                )
        );

        ChartTools.writeTimeChart(
                getStatsBasePath().getPath(), "avg-individual-expenses", "Average of monthly expenses",
                Arrays.asList("Total", "Discretionary", "Non discretionary", "Taxes"),
                Arrays.asList(
                        buildAvgSeries(EXPENSES_TOTAL),
                        buildAvgSeries(EXPENSES_DISCRETIONARY),
                        buildAvgSeries(EXPENSES_NON_DISCRETIONARY),
                        buildAvgSeries(TAXES_TOTAL)
                )
        );

        ChartTools.writeTimeChart(
                getStatsBasePath().getPath(), "sum-individual-expenses", "Sum of monthly expenses",
                Arrays.asList("Total", "Discretionary", "Non discretionary", "Taxes"),
                Arrays.asList(
                        buildSumSeries(EXPENSES_TOTAL),
                        buildSumSeries(EXPENSES_DISCRETIONARY),
                        buildSumSeries(EXPENSES_NON_DISCRETIONARY),
                        buildSumSeries(TAXES_TOTAL)
                )
        );

        ChartTools.writeTimeChart(
                getStatsBasePath().getPath(), "avg-individual-assets", "Average of individual assets",
                Arrays.asList("Savings"),
                Arrays.asList(
                        buildAvgSeries(ASSETS_SAVINGS)
                )
        );

        ChartTools.writeTimeChart(
                getStatsBasePath().getPath(), "sum-individual-assets", "Sum of individual assets",
                Arrays.asList("Savings"),
                Arrays.asList(
                        buildSumSeries(ASSETS_SAVINGS)
                )
        );

        // Salary distribution
        ChartTools.writeHistoChart(getStatsBasePath().getPath(),
                "dist-grossMonthlySalary-income",
                "Salary distribution",
                histogram(p -> p.isAlive() && p.monthlyData.monthIncomeWage > 0, p -> (long) (p.monthlyData.monthIncomeWage / 100)));

        ChartTools.writeHistoChart(getStatsBasePath().getPath(),
                "dist-total-income",
                "Total income distribution",
                histogram(p -> p.isAlive() && p.monthlyData.getTotalMonthIncome() > 0, p -> (long) (p.monthlyData.getTotalMonthIncome() / 100)));

        // TODO: Unemployment, activity rate,

        // TODO: Average wage, pension, subsidy
    }
}
