package com.openworldsimulator.simulation;

import com.openworldsimulator.model.Population;
import com.openworldsimulator.model.PopulationSegment;

import java.io.File;
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

    // Collected monthly statss
    private List<Map<String, DoubleSummaryStatistics>> collectedMonthlyStats = new ArrayList<>();


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

    protected List<PopulationSegment> allPeople() {
        return getPopulation().getPopulationSegments();
    }

    protected Stream<PopulationSegment> allPeopleStream() {
        return getPopulation().getPopulationSegments().stream();
    }

    protected Stream<PopulationSegment> allPeopleAliveStream() {
        return getPopulation().getPopulationSegments().stream().filter(PopulationSegment::isAlive);
    }

    protected DoubleSummaryStatistics statsPopulation(boolean onlyAlive, ToDoubleFunction<PopulationSegment> numericStat) {
        return (onlyAlive ? allPeopleAliveStream() : allPeopleStream()).mapToDouble(numericStat).summaryStatistics();
    }

    protected DoubleSummaryStatistics statsPopulation(boolean onlyAlive, Predicate<PopulationSegment> filter, ToDoubleFunction<PopulationSegment> numericStat) {
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
        currentMonthStats = new HashMap<>();
    }

    protected void collectMonthStats(String statName, boolean onlyAlive, ToDoubleFunction<PopulationSegment> selectorStat) {
        currentMonthStats.put(
                statName,
                statsPopulation(onlyAlive, selectorStat)
        );
    }


    protected void collectMonthStats(String statName, boolean onlyAlive, Predicate<PopulationSegment> filter, ToDoubleFunction<PopulationSegment> selectorStat) {
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
        return collectedMonthlyStats.stream().map(t -> t.get(statId).getSum()).collect(Collectors.toList());
    }

    protected List<Number> buildAvgSeries(String statId) {
        return collectedMonthlyStats.stream().map(
                t -> t.get(statId).getAverage()
        ).collect(Collectors.toList());
    }

    protected List<Number> buildCountSeries(String statId) {
        return collectedMonthlyStats.stream().map(
                t -> t.get(statId).getCount()
        ).collect(Collectors.toList());
    }

    protected String getChartTitle(String chartName) {
        return "[" + simulation.getSimulationId() + "] " + chartName;
    }

    protected <T> Map<Long, Long> histogram(Predicate<PopulationSegment> filter, Function<PopulationSegment, Long> fieldToLong) {
        return histogram(filter, fieldToLong, 1.0D);
    }

    protected <T> Map<Long, Long> histogram(Predicate<PopulationSegment> filter, Function<PopulationSegment, Long> fieldToLong, double scaling) {
        Map<Long, Long> histogram = getPopulation().getPopulationSegments().stream().filter(filter).collect(
                Collectors.groupingBy(
                        fieldToLong,
                        TreeMap::new,
                        Collectors.counting()
                )
        );

        if( scaling != 1.0D) {
            // Apply scaling
            histogram.replaceAll(
                    (k, v) -> (long) ((double) v * scaling)
            );
        }

        return histogram;
    }

    public void writeSnapshots(int month) {
    }

    public void writeChartsAtStart() {
    }

    public void writeChartsAtEnd() {
    }
}