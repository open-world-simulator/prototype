package com.openworldsimulator.demographics;

import com.openworldsimulator.model.Person;
import com.openworldsimulator.simulation.ModelStats;
import com.openworldsimulator.simulation.Simulation;
import com.openworldsimulator.tools.ChartTools;

import java.io.File;
import java.util.Arrays;

public class DemographicsStats extends ModelStats {
    private static final String POPULATION = "population";
    private static final String POPULATION_SIZE_0_15 = "populationSize0_15";
    private static final String POPULATION_SIZE_16_35 = "populationSize16_35";
    private static final String POPULATION_SIZE_36_65 = "populationSize36_65";
    private static final String POPULATION_SIZE_65_PLUS = "populationSize65";
    private static final String AGE = "age";
    private static final String NUM_CHILDREN = "numChildren";
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

        // Collect monthly stats over all population
        collectMonthStats(POPULATION, true, p -> 1);
        collectMonthStats(POPULATION_SIZE_0_15, true, p -> p.age < 16.0, p -> 1);
        collectMonthStats(POPULATION_SIZE_16_35, true, p -> p.age >= 16 && p.age < 35.0, p -> 1);
        collectMonthStats(POPULATION_SIZE_36_65, true, p -> p.age >= 35 && p.age < 65.0, p -> 1);
        collectMonthStats(POPULATION_SIZE_65_PLUS, true, p -> p.age > 65.0, p -> 1);
        collectMonthStats(AGE, true, p->true, p -> p.age);
        collectMonthStats(NUM_CHILDREN, true, p -> p.isFemale() && p.age > params.MATERNITY_MAX_AGE, p -> p.numChildren);

        endMonthStats();
    }

    @Override
    public void writeSnapshots(int month) {
        // Save snapshot if required
        if (month % 120 == 0 || month == 1) {
            File path = getStatsDir("snapshots");

            ChartTools.writeHistoChart(path.getPath(),
                    "dist-age-" + (month / 12),
                    "Age at month at year " + ((int) (month / 12)),
                    histogram(p -> p.isAlive(), p -> (long) (p.age)));
        }
    }

    @Override
    public void writeChartsAtStart() {
        File initialPath = getStatsDir("initial");

        ChartTools.writeHistoChart(initialPath.getPath(),
                "dist-initial-life-expectancy",
                "Life expectancy at birth",
                histogram(p -> true, p -> (long) (p.initialLifeExpectancy)));

        ChartTools.writeHistoChart(initialPath.getPath(),
                "dist-initial-age",
                "Initial age",
                histogram(p -> true, p -> (long) (p.age)));


        ChartTools.writeHistoChart(initialPath.getPath(),
                "dist-expected-children",
                "Expected children",
                histogram(Person::isFemale, p -> (long) (p.initialExpectedChildren)));

        ChartTools.writeHistoChart(initialPath.getPath(),
                "dist-expected-first-born-age",
                "Expected age of first born",
                histogram(Person::isFemale, p -> (long) (p.initialFirstChildAge)));

    }

    @Override
    public void writeChartsAtEnd() {

        File demographyPath = getStatsBasePath();
        File birthsPath = getStatsDir("births");
        File deathsPath = getStatsDir("deaths");

        //
        // Write time charts
        //
        ChartTools.writeTimeChart(
                demographyPath.getPath(),
                POPULATION,
                "Population",
                Arrays.asList(
                        "Population",
                        "0-15",
                        "16-35",
                        "36-65",
                        "65+"
                ),
                Arrays.asList(
                        buildCountSeries(POPULATION),
                        buildCountSeries(POPULATION_SIZE_0_15),
                        buildCountSeries(POPULATION_SIZE_16_35),
                        buildCountSeries(POPULATION_SIZE_36_65),
                        buildCountSeries(POPULATION_SIZE_65_PLUS)
                ),
                getSimulation().getBaseYear()
        );

/*   ChartTools.writeTimeChartYoYPercent(
                demographyPath.getPath(),
                "population_pct",
                "Population",
                Arrays.asList(
                        "Population"
                ),
                Arrays.asList(
                        buildCountSeries(POPULATION)
                )
        );
*/
        // TODO: Population growth

        ChartTools.writeTimeChart(demographyPath.getPath(), "age", "Average Age of population",
                buildAvgSeries("age"), getSimulation().getBaseYear());

        ChartTools.writeTimeChart(demographyPath.getPath(), "fertility", "Average children per woman",
                buildAvgSeries("numChildren"), getSimulation().getBaseYear());

        //
        // Write histograms
        //
        ChartTools.writeHistoChart(
                deathsPath.getPath(),
                "dist-age-of-death",
                "Death age",
                histogram(p -> !p.isAlive(), p -> (long) (p.age)));

        ChartTools.writeHistoChart(birthsPath.getPath(),
                "dist-births-mother-age",
                "Age of mother at birth",
                histogram(p -> p.mothersAgeAtBirth > 0, p -> (long) (p.mothersAgeAtBirth)));

        ChartTools.writeHistoChart(birthsPath.getPath(),
                "dist-num-children-woman",
                "Number of children per woman",
                histogram(p -> p.isFemale(), p -> (long) (p.numChildren)));


        ChartTools.writeHistoChart(demographyPath.getPath(),
                "dist-age-final",
                "Final distribution of age",
                histogram(Person::isAlive, p -> (long) (p.age)));
    }
}
