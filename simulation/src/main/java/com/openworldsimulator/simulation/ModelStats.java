package com.openworldsimulator.simulation;

import com.openworldsimulator.model.Person;
import com.openworldsimulator.model.Population;
import com.openworldsimulator.tools.CSVTools;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class ModelStats {

    // Simulation this model belongs to
    private Simulation simulation;

    // Current Month Stats
    private Map<String, DoubleSummaryStatistics> currentMonthStats = new HashMap<>();

    // Collected monthly stats
    private List<Map<String, DoubleSummaryStatistics>> collectedMonthlyStats = new ArrayList<>();

    // Calculated aggregated series
    private Map<String, List<Number>> aggregatedSeries = new TreeMap<>();

    public ModelStats(Simulation simulation) {
        this.simulation = simulation;
    }

    public Simulation getSimulation() {
        return simulation;
    }

    public Population getPopulation() {
        return simulation.getPopulation();
    }

    protected abstract String getStatsId();

    //
    // File output helper
    //
    protected File getStatsBasePath() {
        File path = new File(getSimulation().getSimulationOutputPath(), getStatsId());
        path.mkdirs();
        return path;
    }

    protected File getStatsDir(String subdir) {
        File f = new File(getStatsBasePath(), subdir);
        f.mkdirs();
        return f;
    }

    public Map<String, DoubleSummaryStatistics> getCurrentMonthStats() {
        return currentMonthStats;
    }

    // Iterators over population

    protected List<Person> allPeople() {
        return getPopulation().getPopulationSegments();
    }

    protected Stream<Person> allPeopleStream() {
        return getPopulation().getPopulationSegments().stream();
    }

    protected Stream<Person> allPeopleAliveStream() {
        return getPopulation().getPopulationSegments().stream().filter(Person::isAlive);
    }

    protected DoubleSummaryStatistics statsPopulation(boolean onlyAlive, ToDoubleFunction<Person> numericStat) {
        return (onlyAlive ? allPeopleAliveStream() : allPeopleStream()).mapToDouble(numericStat).summaryStatistics();
    }

    protected DoubleSummaryStatistics statsPopulation(boolean onlyAlive, Predicate<Person> filter, ToDoubleFunction<Person> numericStat) {
        return (onlyAlive ? allPeopleAliveStream() : allPeopleStream()).filter(filter).mapToDouble(numericStat).summaryStatistics();
    }

    protected void beginMonthStats() {
        currentMonthStats = new HashMap<>();
    }

    protected void endMonthStats() {
        collectedMonthlyStats.add(currentMonthStats);
    }

    public void clearMonthStats() {
        collectedMonthlyStats.clear();
        aggregatedSeries.clear();
        currentMonthStats = new HashMap<>();
    }

    protected void collectMonthStats(String statName, boolean onlyAlive, ToDoubleFunction<Person> selectorStat) {
        currentMonthStats.put(
                statName,
                statsPopulation(onlyAlive, selectorStat)
        );
    }


    protected void collectMonthStats(String statName, boolean onlyAlive, Predicate<Person> filter, ToDoubleFunction<Person> selectorStat) {
        currentMonthStats.put(
                statName,
                statsPopulation(onlyAlive, filter, selectorStat)
        );
    }

    protected void collectMonthStats(String statName, double value) {
        DoubleSummaryStatistics summaryStatistics = new DoubleSummaryStatistics();
        summaryStatistics.accept(value);
        currentMonthStats.put(
                statName,
                summaryStatistics
        );
    }

    protected List<Number> buildSumSeries(String statId) {
        return buildSumSeries(statId, 1.0);
    }

    protected List<Number> buildSumSeries(String statId, double scaleFactor) {
        List<Number> series = collectedMonthlyStats.stream().map(t -> t.get(statId).getSum()*scaleFactor).collect(Collectors.toList());

        aggregatedSeries.put(statId+"_sum", series);
        return series;
    }

    protected List<Number> buildAvgSeries(String statId) {
        List<Number> series = collectedMonthlyStats.stream().map(
                t -> t.get(statId).getAverage()
        ).collect(Collectors.toList());

        aggregatedSeries.put(statId+"_avg", series);
        return series;
    }

    protected List<Number> buildCountSeries(String statId) {
        List<Number> series = collectedMonthlyStats.stream().map(
                t -> t.get(statId).getCount()
        ).collect(Collectors.toList());

        aggregatedSeries.put(statId+"_count", series);
        return series;
    }

    protected Map<String, List<Number>> getAggregatedSeries() {
        return aggregatedSeries;
    }

    protected String getChartTitle(String chartName) {
        return "[" + simulation.getSimulationId() + "] " + chartName;
    }

    protected <T> Map<Long, Long> histogram(Predicate<Person> filter, Function<Person, Long> fieldToLong) {
        return histogram(filter, fieldToLong, 1.0D);
    }

    protected <T> Map<Long, Long> histogram(Predicate<Person> filter, Function<Person, Long> fieldToLong, double scaling) {
        Map<Long, Long> histogram = getPopulation().getPopulationSegments().stream().filter(filter).collect(
                Collectors.groupingBy(
                        fieldToLong,
                        TreeMap::new,
                        Collectors.counting()
                )
        );

        if (scaling != 1.0D) {
            // Apply scaling
            histogram.replaceAll(
                    (k, v) -> (long) ((double) v * scaling)
            );
        }

        return histogram;
    }

    public void writeAllAggregatedTimeSeriesCSV(String file, int baseYear) throws IOException {

        List<List<Number>> series = new ArrayList<>();
        List<String> seriesNames = new ArrayList<>();
        // Add series
        for (String k : getAggregatedSeries().keySet()) {
            seriesNames.add(k);
            series.add(getAggregatedSeries().get(k));
        }

        CSVTools.writeCSV(
                new File(getStatsBasePath(), file),
                baseYear,
                seriesNames,
                series
        );
    }

    public abstract void collect(int month);

    public void writeSnapshots(int month) {
    }

    public void writeChartsAtStart() {
    }

    public void writeChartsAtEnd() {
    }
}