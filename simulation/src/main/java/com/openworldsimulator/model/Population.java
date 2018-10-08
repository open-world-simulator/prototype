package com.openworldsimulator.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Population {
    private long realPopulationSize;
    private long initialPopulationSegments;

    private List<PopulationSegment> people = new ArrayList<>();

    public Population() {
    }

    public void clear() {
        people.clear();
        realPopulationSize = 0;
        initialPopulationSegments = 0;
    }

    public void add(PopulationSegment p) {
        people.add(p);
    }

    public int size() {
        return people.size();
    }

    public long getRealPopulationSize() {
        return realPopulationSize;
    }

    public void setRealPopulationSize(long realPopulationSize) {
        this.realPopulationSize = realPopulationSize;
    }

    public long getInitialPopulationSegments() {
        return initialPopulationSegments;
    }

    public void setInitialPopulationSegments(long initialPopulationSegments) {
        this.initialPopulationSegments = initialPopulationSegments;
    }

    public double getSegmentRepresentationRatio() {
        return (double) realPopulationSize / (double) initialPopulationSegments;
    }

    public List<PopulationSegment> getPopulationSegments() {
        return people;
    }

    public List<PopulationSegment> getAlivePeople() {
        return people.stream().filter(PopulationSegment::isAlive).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "Population { " +
                "realPopulationSize=" + realPopulationSize +
                ", initialPopulationSegments=" + initialPopulationSegments +
                ", size=" + people.size() +
                ", ratio=" + String.format("%.1f", getSegmentRepresentationRatio()) + " people " +
                '}';
    }
}
