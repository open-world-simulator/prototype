package com.openworldsimulator.tools;

import com.google.gson.Gson;
import com.openworldsimulator.demographics.DemographicParams;
import com.openworldsimulator.model.PopulationSegment;
import com.openworldsimulator.model.Population;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InitialPopulationLoader {
    private String country;
    private int year;
    private int targetPopulationSize;
    private DemographicParams params;
    private Population population;

    public InitialPopulationLoader(Population population, DemographicParams params, String country, int year, int targetPopulationSize) {
        this.country = country;
        this.year = year;
        this.targetPopulationSize = targetPopulationSize;
        this.params = params;
        this.population = population;
    }

    public Population load() throws IOException {

        String endpoint = "http://api.population.io/1.0/population/" + year + "/" + country + "/";

        InputStream is = new URL(endpoint).openStream();

        try {
            System.out.println("Loading population from: " + endpoint);

            String json = JSONTools.readJson(endpoint);

            List<Map<String, Double>> populationData = new ArrayList<>();
            populationData = new Gson().fromJson(json, populationData.getClass());

            int totalPopulation = populationData.stream().collect(Collectors.summingInt(e -> e.get("total").intValue())).intValue();

            double ratio = (double) targetPopulationSize / totalPopulation;

            System.out.println("Total population:     " + totalPopulation);
            System.out.println("Requested population: " + targetPopulationSize);

            populationData.forEach(e -> {
                int females = e.get("females").intValue();
                int males = e.get("males").intValue();
                int age = e.get("age").intValue();

                createPeople(population, ratio, females, age, PopulationSegment.GENDER.FEMALE);
                createPeople(population, ratio, males, age, PopulationSegment.GENDER.MALE);

            });

            population.setRealPopulationSize(totalPopulation);
            population.setInitialPopulationSegments(population.size());

            System.out.println("Created population: " + population.size());

        } finally {
            is.close();
        }

        return population;
    }

    private void createPeople(Population population, double ratio, int people, int age, PopulationSegment.GENDER gender) {
        int n = (int) Math.round((double) people * ratio);

        //System.out.println("Age: " + age + " Creating " + n + " " + gender);

        for (int i = 0; i < n; i++) {
            population.add(
                    initPerson(
                            null,
                            population.size(),
                            gender,
                            age + RandomTools.random() / 365D
                    )
            );
        }
    }

    private PopulationSegment initPerson(PopulationSegment populationSegment, int id, PopulationSegment.GENDER gender, double age) {

        if (populationSegment == null) {
            populationSegment = new PopulationSegment();
        }
        populationSegment.id = id;
        populationSegment.gender = gender;
        populationSegment.status = PopulationSegment.STATUS.ALIVE;

        populationSegment.age = age;

        populationSegment.bornMonth = -1;
        populationSegment.numChildren = 0;

        // Update vars for fertility model
        if (populationSegment.isFemale()) {
            populationSegment.initialFirstChildAge =
                    RandomTools.random(
                            params.MATERNITY_AGE_MEAN,
                            params.MATERNITY_AGE_STDEV,
                            params.MATERNITY_MIN_AGE,
                            params.MATERNITY_MAX_AGE);

            populationSegment.initialExpectedChildren =
                    (int) Math.abs(RandomTools.random(
                            params.MATERNITY_NUM_CHILDREN_MEAN,
                            params.MATERNITY_NUM_CHILDREN_STDEV));

            if( populationSegment.age >= populationSegment.initialFirstChildAge ) {
                populationSegment.numChildren = populationSegment.initialExpectedChildren;
            }
        }

        populationSegment.initialLifeExpectancy = RandomTools.random(
                params.INITIAL_LIFE_EXPECTANCY_MEAN,
                params.INITIAL_LIFE_EXPECTANCY_STDEV,
                populationSegment.age,
                params.INITIAL_LIFE_EXPECTANCY_MAX);

        return populationSegment;
    }
}
