package com.openworldsimulator.tools;

import com.google.gson.Gson;
import com.openworldsimulator.demographics.DemographicParams;
import com.openworldsimulator.model.Person;
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

                createPeople(population, ratio, females, age, Person.GENDER.FEMALE);
                createPeople(population, ratio, males, age, Person.GENDER.MALE);

            });

            population.setRealPopulationSize(totalPopulation);
            population.setInitialPopulationSegments(population.size());

            System.out.println("Created population: " + population.size());

        } finally {
            is.close();
        }

        return population;
    }

    private void createPeople(Population population, double ratio, int people, int age, Person.GENDER gender) {
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

    private Person initPerson(Person person, int id, Person.GENDER gender, double age) {

        if (person == null) {
            person = new Person();
        }
        person.id = id;
        person.gender = gender;
        person.status = Person.STATUS.ALIVE;

        person.age = age;

        person.bornMonth = -1;
        person.numChildren = 0;

        // Update vars for fertility model
        if (person.isFemale()) {
            person.initialFirstChildAge =
                    RandomTools.random(
                            params.MATERNITY_AGE_MEAN,
                            params.MATERNITY_AGE_STDEV,
                            params.MATERNITY_MIN_AGE,
                            params.MATERNITY_MAX_AGE);

            person.initialExpectedChildren =
                    (int) Math.abs(RandomTools.random(
                            params.MATERNITY_NUM_CHILDREN_MEAN,
                            params.MATERNITY_NUM_CHILDREN_STDEV));

            if( person.age >= person.initialFirstChildAge ) {
                person.numChildren = person.initialExpectedChildren;
            }
        }

        person.initialLifeExpectancy = RandomTools.random(
                params.INITIAL_LIFE_EXPECTANCY_MEAN,
                params.INITIAL_LIFE_EXPECTANCY_STDEV,
                person.age,
                params.INITIAL_LIFE_EXPECTANCY_MAX);

        return person;
    }
}
