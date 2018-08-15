package com.openworldsimulator.simulation;

import com.openworldsimulator.model.Person;
import com.openworldsimulator.model.Population;

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


    // Iterators over population

    protected List<Person> allPeople() {
        return getPopulation().getPeople();
    }

    protected Stream<Person> allPeopleStream() {
        return getPopulation().getPeople().stream();
    }

    protected Stream<Person> allPeopleAliveStream() {
        return getPopulation().getPeople().stream().filter(Person::isAlive);
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
        currentMonthStats = new HashMap<>();
    }

    protected void collectMonthStats(String statName, boolean onlyAlive, ToDoubleFunction<Person> selectorStat) {
        currentMonthStats.put(
                statName,
                statsPopulation(onlyAlive, selectorStat)
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

    protected void collectMonthStats(String statName, boolean onlyAlive, Predicate<Person> filter, ToDoubleFunction<Person> selectorStat) {
        currentMonthStats.put(
                statName,
                statsPopulation(onlyAlive, filter, selectorStat)
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


    protected <T> Map<Long, Long> histogram(Predicate<Person> filter, Function<Person, Long> fieldToLong) {
        return getPopulation().getPeople().stream().filter(filter).collect(
                Collectors.groupingBy(
                        fieldToLong,
                        TreeMap::new,
                        Collectors.counting()
                )
        );
    }

    public void writeSnapshots(int month) {
    }

    public void writeChartsAtStart() {
    }

    public void writeChartsAtEnd() {
    }
}