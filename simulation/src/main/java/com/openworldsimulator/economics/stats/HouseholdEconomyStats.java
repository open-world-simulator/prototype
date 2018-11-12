package com.openworldsimulator.economics.stats;

import com.openworldsimulator.simulation.ModelStats;
import com.openworldsimulator.simulation.Simulation;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static com.openworldsimulator.tools.charts.TimeSeriesChartTools.writeTimeSeriesChart;

public class HouseholdEconomyStats extends ModelStats {

    private static final String INCOME_WAGE = "household_income_wages";
    private static final String INCOME_PENSION = "household_income_pension";
    private static final String INCOME_FINANCIAL = "household_income_financial";
    private static final String INCOME_TOTAL = "household_income_total";

    private static final String INCOME_16_35 = "household_income_16_35";
    private static final String INCOME_36_65 = "household_income_36_65";
    private static final String INCOME_65_PLUS = "household_income_65";

    private static final String EXPENSES_TOTAL = "household_expenses_total";
    private static final String CONSUMPTION_DISCRETIONARY = "household_consumption_discretionary";
    private static final String CONSUMPTION_NON_DISCRETIONARY = "household_consumption_non_discretionary";
    private static final String CONSUMPTION_TOTAL = "household_consumption_total";
    private static final String TAXES = "household_expenses_taxes";

    private static final String CONSUMPTION_16_35 = "household_consumption_16_35";
    private static final String CONSUMPTION_36_65 = "household_consumption_36_65";
    private static final String CONSUMPTION_65_PLUS = "household_consumption_65";

    private static final String NET_SAVINGS = "household_net_savings";
    private static final String NET_SAVINGS_16_35 = "household_net_savings_16_35";
    private static final String NET_SAVINGS_36_65 = "household_net_savings_36_65";
    private static final String NET_SAVINGS_65_PLUS = "household_net_savings_65";

    public HouseholdEconomyStats(Simulation simulation) {
        super(simulation);
    }

    @Override
    protected String getStatsId() {
        return "household";
    }


    public void collect(int month) {

        beginMonthStats();

        // Collect monthly stats over all population
        collectMonthStats(INCOME_WAGE, true, p -> p.monthlyData.incomeWage);
        collectMonthStats(INCOME_PENSION, true, p -> p.monthlyData.incomePension);
        collectMonthStats(INCOME_FINANCIAL, true, p -> p.monthlyData.incomeSavings);

        collectMonthStats(INCOME_TOTAL, true, p -> p.monthlyData.getTotalMonthIncome());
        collectMonthStats(INCOME_16_35, true, p -> p.age >= 16.0 && p.age < 36, p -> p.monthlyData.getTotalMonthIncome());
        collectMonthStats(INCOME_36_65, true, p -> p.age >= 36 && p.age < 65, p -> p.monthlyData.getTotalMonthIncome());
        collectMonthStats(INCOME_65_PLUS, true, p -> p.age >= 65, p -> p.monthlyData.getTotalMonthIncome());

        collectMonthStats(EXPENSES_TOTAL, true, p -> p.monthlyData.getTotalExpenses());
        collectMonthStats(TAXES, true, p -> p.monthlyData.getTotalTaxes());
        collectMonthStats(CONSUMPTION_TOTAL, true, p -> p.monthlyData.getTotalConsumption());
        collectMonthStats(CONSUMPTION_DISCRETIONARY, true, p -> p.monthlyData.consumptionDiscretionary);
        collectMonthStats(CONSUMPTION_NON_DISCRETIONARY, true, p -> p.monthlyData.consumptionNonDiscretionary);

        collectMonthStats(CONSUMPTION_16_35, true, p -> p.age >= 16.0 && p.age < 36, p -> p.monthlyData.getTotalConsumption());
        collectMonthStats(CONSUMPTION_36_65, true, p -> p.age >= 36 && p.age < 65, p -> p.monthlyData.getTotalConsumption());
        collectMonthStats(CONSUMPTION_65_PLUS, true, p -> p.age >= 65, p -> p.monthlyData.getTotalConsumption());

        collectMonthStats(NET_SAVINGS, true, p -> p.monthlyData.getTotalMonthNetResult());
        collectMonthStats(NET_SAVINGS_16_35, true, p -> p.age >= 16.0 && p.age < 36, p -> p.monthlyData.getTotalMonthNetResult());
        collectMonthStats(NET_SAVINGS_36_65, true, p -> p.age >= 36 && p.age < 65, p -> p.monthlyData.getTotalMonthNetResult());
        collectMonthStats(NET_SAVINGS_65_PLUS, true, p -> p.age >= 65, p -> p.monthlyData.getTotalMonthNetResult());

        endMonthStats();
    }


    @Override
    public void writeChartsAtEnd() {

        try {

            // To convert to real magnitudes
            double populationScalingFactor = getPopulation().getSegmentRepresentationRatio();

            File incomePath = getStatsDir("income");
            File expensesPath = getStatsDir("expenses");

            //
            // Income stats
            //
            writeTimeSeriesChart(
                    incomePath.getPath(), "income-avg",
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
                    incomePath.getPath(), "income-avg-by-age-group",
                    getChartTitle("Average of monthly income per age"),
                    "Average of monthly income",
                    Arrays.asList("16-36", "36-65", "65+"),
                    Arrays.asList(
                            buildAvgSeries(INCOME_16_35),
                            buildAvgSeries(INCOME_36_65),
                            buildAvgSeries(INCOME_65_PLUS)
                    )
                    ,
                    getSimulation().getBaseYear()
            );

            writeTimeSeriesChart(
                    incomePath.getPath(), "income-sum",
                    getChartTitle("Sum of monthly income"),
                    "Sum of monthly income",
                    Arrays.asList("Total", "Wages", "Pensions", "Financial"),
                    Arrays.asList(
                            buildSumSeries(INCOME_TOTAL, populationScalingFactor),
                            buildSumSeries(INCOME_WAGE, populationScalingFactor),
                            buildSumSeries(INCOME_PENSION, populationScalingFactor),
                            buildSumSeries(INCOME_FINANCIAL, populationScalingFactor)
                    )
                    ,
                    getSimulation().getBaseYear()
            );

            writeTimeSeriesChart(
                    incomePath.getPath(), "income-sum-by-age-group",
                    getChartTitle("Sum of monthly income per age group"),
                    "Sum of monthly income",
                    Arrays.asList("16-36", "36-65", "65+"),
                    Arrays.asList(
                            buildSumSeries(INCOME_16_35, populationScalingFactor),
                            buildSumSeries(INCOME_36_65, populationScalingFactor),
                            buildSumSeries(INCOME_65_PLUS, populationScalingFactor)
                    )
                    ,
                    getSimulation().getBaseYear()
            );


            //
            // Consumption stats
            //

            writeTimeSeriesChart(
                    expensesPath.getPath(), "expenses-avg",
                    getChartTitle("Average of household expenses"),
                    "Average of expenses",
                    Arrays.asList("Total expenses", "Consumption", "Discretionary consumption", "Non discretionary consumption", "Taxes"),
                    Arrays.asList(
                            buildAvgSeries(EXPENSES_TOTAL),
                            buildAvgSeries(CONSUMPTION_TOTAL),
                            buildAvgSeries(CONSUMPTION_DISCRETIONARY),
                            buildAvgSeries(CONSUMPTION_NON_DISCRETIONARY),
                            buildAvgSeries(TAXES)
                    )
                    ,
                    getSimulation().getBaseYear()
            );

            writeTimeSeriesChart(
                    expensesPath.getPath(), "expenses-sum",
                    getChartTitle("Sum of household expenses"),
                    "Sum of expenses",
                    Arrays.asList("Total expenses", "Consumption", "Discretionary consumption", "Non discretionary consumption", "Taxes"),
                    Arrays.asList(
                            buildSumSeries(EXPENSES_TOTAL, populationScalingFactor),
                            buildSumSeries(CONSUMPTION_TOTAL, populationScalingFactor),
                            buildSumSeries(CONSUMPTION_DISCRETIONARY, populationScalingFactor),
                            buildSumSeries(CONSUMPTION_NON_DISCRETIONARY, populationScalingFactor),
                            buildSumSeries(TAXES, populationScalingFactor)
                    )
                    ,
                    getSimulation().getBaseYear()
            );

            writeTimeSeriesChart(
                    expensesPath.getPath(), "consumption-avg-by-age-group",
                    getChartTitle("Average of consumption by age group"),
                    "Average of consumption",
                    Arrays.asList("Total", "16-35", "36-65", "65+"),
                    Arrays.asList(
                            buildAvgSeries(CONSUMPTION_TOTAL),
                            buildAvgSeries(CONSUMPTION_16_35),
                            buildAvgSeries(CONSUMPTION_36_65),
                            buildAvgSeries(CONSUMPTION_65_PLUS)
                    )
                    ,
                    getSimulation().getBaseYear()
            );

            writeTimeSeriesChart(
                    expensesPath.getPath(), "consumption-sum-by-age-group",
                    getChartTitle("Sum of consumption by age group"),
                    "Total of consumption",
                    Arrays.asList("Total", "16-35", "36-65", "65+"),
                    Arrays.asList(
                            buildSumSeries(CONSUMPTION_TOTAL),
                            buildSumSeries(CONSUMPTION_16_35),
                            buildSumSeries(CONSUMPTION_36_65),
                            buildSumSeries(CONSUMPTION_65_PLUS)
                    )
                    ,
                    getSimulation().getBaseYear()
            );

            //
            // Net result
            //
            writeTimeSeriesChart(
                    getStatsBasePath().getPath(), "net-savings-avg",
                    getChartTitle("Net savings avg"),
                    "Average of net savings",
                    Arrays.asList("Total", "16-35", "36-65", "65+"),
                    Arrays.asList(
                            buildAvgSeries(NET_SAVINGS),
                            buildAvgSeries(NET_SAVINGS_16_35),
                            buildAvgSeries(NET_SAVINGS_36_65),
                            buildAvgSeries(NET_SAVINGS_65_PLUS)
                    )
                    ,
                    getSimulation().getBaseYear()
            );

            writeTimeSeriesChart(
                    getStatsBasePath().getPath(), "net-savings-sum",
                    getChartTitle("Net savings total"),
                    "Sum of net savings",
                    Arrays.asList("Total", "16-35", "36-65", "65+"),
                    Arrays.asList(
                            buildSumSeries(NET_SAVINGS, populationScalingFactor),
                            buildSumSeries(NET_SAVINGS_16_35, populationScalingFactor),
                            buildSumSeries(NET_SAVINGS_36_65, populationScalingFactor),
                            buildSumSeries(NET_SAVINGS_65_PLUS, populationScalingFactor)
                    )
                    ,
                    getSimulation().getBaseYear()
            );


            /*
            // Salary distribution
            writeHistoChart(getStatsBasePath().getPath(),
                    "dist-grossMonthlySalary-income",
                    "Salary distribution",
                    histogram(p -> p.isAlive() && p.monthlyData.incomeWage > 0, p -> (long) (p.monthlyData.incomeWage / 100)));

            writeHistoChart(getStatsBasePath().getPath(),
                    "dist-total-income",
                    "Total income distribution",
                    histogram(p -> p.isAlive() && p.monthlyData.getTotalMonthIncome() > 0, p -> (long) (p.monthlyData.getTotalMonthIncome() / 100)));

            // TODO: Unemployment, activity rate,
            // TODO: Average wage, pension, subsidy
*/
            // Write CSV
            writeAllAggregatedTimeSeriesCSV("series.csv", getSimulation().getBaseYear());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
