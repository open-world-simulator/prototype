package com.openworldsimulator.demographics;

import com.openworldsimulator.model.PopulationSegment;
import com.openworldsimulator.simulation.ModelStats;
import com.openworldsimulator.simulation.Simulation;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static com.openworldsimulator.tools.HistogramChartTools.writeHistoChart;
import static com.openworldsimulator.tools.TimeSeriesChartTools.writeTimeSeriesChart;

public class DemographicsStats extends ModelStats {
    private static final String POPULATION = "population";
    private static final String POPULATION_SIZE_0_15 = "populationSize0_15";
    private static final String POPULATION_SIZE_16_35 = "populationSize16_35";
    private static final String POPULATION_SIZE_36_65 = "populationSize36_65";
    private static final String POPULATION_SIZE_65_PLUS = "populationSize65";
    private static final String POPULATION_IMMIGRATION = "populationImmigration";
    private static final String POPULATION_BIRTHS = "populationNewBorn";
    private static final String POPULATION_DECEASES = "populationDeceases";
    private static final String POPULATION_NET_GROWTH = "populationGrowth";
    private static final String POPULATATION_AGE = "age";
    private static final String POPULATION_NUM_CHILDREN = "numChildren";
    private DemographicParams params;

    public DemographicsStats(Simulation simulation, DemographicParams params) {
        super(simulation);
        this.params = params;
    }

    @Override
    protected String getStatsId() {
        return "demography";
    }

    public void collect(int month) {
        beginMonthStats();

        // To convert to real magnitudes
        double populationScalingFactor = getPopulation().getSegmentRepresentationRatio();

        // Population evolution stats
        collectMonthStats(POPULATION, true, p -> populationScalingFactor);
        collectMonthStats(POPULATION_SIZE_0_15, true, p -> p.age < 16.0, p -> populationScalingFactor);
        collectMonthStats(POPULATION_SIZE_16_35, true, p -> p.age >= 16 && p.age < 35.0, p -> populationScalingFactor);
        collectMonthStats(POPULATION_SIZE_36_65, true, p -> p.age >= 35 && p.age < 65.0, p -> populationScalingFactor);
        collectMonthStats(POPULATION_SIZE_65_PLUS, true, p -> p.age > 65.0, p -> populationScalingFactor);

        collectMonthStats(POPULATION_IMMIGRATION, true, p -> p.immigrationMonth == month, p -> populationScalingFactor * 12.0);
        collectMonthStats(POPULATION_BIRTHS, true, p -> p.justBorn(month), p -> populationScalingFactor * 12.0);
        collectMonthStats(POPULATION_DECEASES, false, p -> p.justDead(month), p -> populationScalingFactor * 12.0);
        collectMonthStats(
                POPULATION_NET_GROWTH,
                getCurrentMonthStats().get(POPULATION_IMMIGRATION).getSum() +
                        getCurrentMonthStats().get(POPULATION_BIRTHS).getSum() -
                        getCurrentMonthStats().get(POPULATION_DECEASES).getSum()
        );

        collectMonthStats(POPULATATION_AGE, true, p -> true, p -> p.age);
        collectMonthStats(POPULATION_NUM_CHILDREN, true, p -> p.isFemale() && p.age > params.MATERNITY_MAX_AGE, p -> p.numChildren);


        endMonthStats();
    }

    @Override
    public void writeSnapshots(int month) {
        // Save snapshot if required
        if (month % 120 == 0 || month == 1) {
            File path = getStatsDir("snapshots");

            writeHistoChart(path.getPath(),
                    "dist-age-" + (month / 12),
                    "Age at month at year " + ((int) (month / 12)),
                    histogram(p -> p.isAlive(), p -> (long) (p.age)));
        }
    }

    @Override
    public void writeChartsAtStart() {
        File initialPath = getStatsDir("initial");

        double populationScalingFactor = getPopulation().getSegmentRepresentationRatio();

        writeHistoChart(initialPath.getPath(),
                "dist-initial-life-expectancy",
                "Life expectancy at birth",
                histogram(p -> true, p -> (long) (p.initialLifeExpectancy)));

        writeHistoChart(initialPath.getPath(),
                "dist-initial-age",
                "Initial age",
                histogram(p -> true, p -> (long) (p.age), populationScalingFactor));


        writeHistoChart(initialPath.getPath(),
                "dist-expected-children",
                "Expected children",
                histogram(PopulationSegment::isFemale, p -> (long) (p.initialExpectedChildren)));

        writeHistoChart(initialPath.getPath(),
                "dist-expected-first-born-age",
                "Expected age of first born",
                histogram(PopulationSegment::isFemale, p -> (long) (p.initialFirstChildAge)));

    }

    @Override
    public void writeChartsAtEnd() {

        File demographyPath = getStatsBasePath();
        File birthsPath = getStatsDir("births");
        File deathsPath = getStatsDir("deaths");

        //
        // Write time charts
        //
        try {
            writeTimeSeriesChart(
                    demographyPath.getPath(),
                    "population",
                    getChartTitle("Population segments"),
                    "Population",
                    Arrays.asList(
                            "Population",
                            "0-15",
                            "16-35",
                            "36-65",
                            "65+"
                    ),
                    Arrays.asList(
                            buildSumSeries(POPULATION),
                            buildSumSeries(POPULATION_SIZE_0_15),
                            buildSumSeries(POPULATION_SIZE_16_35),
                            buildSumSeries(POPULATION_SIZE_36_65),
                            buildSumSeries(POPULATION_SIZE_65_PLUS)
                    ),
                    getSimulation().getBaseYear()
            );

            writeTimeSeriesChart(
                    demographyPath.getPath(),
                    "population-flows",
                    getChartTitle("Population flows (annualized)"),
                    "Population Flows",
                    Arrays.asList(
                            "Immigration",
                            "Births",
                            "Deceases",
                            "Net growth"
                    ),
                    Arrays.asList(
                            buildSumSeries(POPULATION_IMMIGRATION),
                            buildSumSeries(POPULATION_BIRTHS),
                            buildSumSeries(POPULATION_DECEASES),
                            buildSumSeries(POPULATION_NET_GROWTH)
                    ),
                    getSimulation().getBaseYear()
            );


/*   TimeSeriesChartTools.writeTimeChartYoYPercent(
                demographyPath.getPath(),
                "population_pct",
                "Population",
                Arrays.asList(
                        "Population"º
                Arrays.asList(
                        buildCountSeries(POPULATION)
                )
        );
*/
            // TODO: Population growth

            writeTimeSeriesChart(demographyPath.getPath(), "age",
                    getChartTitle("Average Age of population"),
                    "Age",
                    buildAvgSeries("age"), getSimulation().getBaseYear());

            writeTimeSeriesChart(demographyPath.getPath(), "fertility",
                    getChartTitle("Average children per woman"),
                    "Children",
                    buildAvgSeries("numChildren"), getSimulation().getBaseYear());

            //
            // Write histograms
            //
            writeHistoChart(
                    deathsPath.getPath(),
                    "dist-age-of-death",
                    "Death age",
                    histogram(p -> !p.isAlive(), p -> (long) (p.age)));

            writeHistoChart(birthsPath.getPath(),
                    "dist-births-mother-age",
                    "Age of mother at birth",
                    histogram(p -> p.mothersAgeAtBirth > 0, p -> (long) (p.mothersAgeAtBirth)));

            writeHistoChart(birthsPath.getPath(),
                    "dist-num-children-woman",
                    "Number of children per woman",
                    histogram(p -> p.isFemale(), p -> (long) (p.numChildren)));


            writeHistoChart(demographyPath.getPath(),
                    "dist-age-final",
                    "Final distribution of age",
                    histogram(PopulationSegment::isAlive, p -> (long) (p.age)));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
