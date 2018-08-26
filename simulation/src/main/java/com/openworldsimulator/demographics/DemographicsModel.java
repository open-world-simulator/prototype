package com.openworldsimulator.demographics;

import com.openworldsimulator.model.Person;
import com.openworldsimulator.simulation.ModelParameters;
import com.openworldsimulator.simulation.ModelStats;
import com.openworldsimulator.simulation.Simulation;
import com.openworldsimulator.simulation.SimulationModel;
import com.openworldsimulator.tools.InitialPopulationLoader;
import com.openworldsimulator.tools.RandomTools;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class DemographicsModel extends SimulationModel {

    public static final String MODEL_ID = "demographics-model";

    private DemographicParams params;

    /**
     * Stats collection
     **/
    private DemographicsStats modelStats;

    public DemographicsModel(Simulation simulation, DemographicParams params, File outputPath) {
        super(outputPath, simulation);

        this.params = params;
    }

    @Override
    public String getId() {
        return MODEL_ID;
    }

    @Override
    public ModelParameters getParams() {
        return params;
    }

    /*
     * Initialization
     *
     */

    @Override
    public void init() {

        super.init();

        modelStats = new DemographicsStats(simulation, params);

        log("*** INITIALIZING INITIAL POPULATION OF " + params.INITIAL_POPULATION_SIZE);

        InitialPopulationLoader loader = new InitialPopulationLoader(
                simulation.getPopulation(),
                params,
                params.INITIAL_DEMOGRAPHY_DATA_COUNTRY,
                params.INITIAL_DEMOGRAPHY_DATA_YEAR,
                (int) params.INITIAL_POPULATION_SIZE
        );

        try {
            loader.load();
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }

        log("Starting runSimulation with population of:" + simulation.getPopulation().size());
    }

    @Override
    public ModelStats getStats() {
        return modelStats;
    }


    /*
     * Simulation
     *
     */

    @Override
    public void preSimulation(int month) {
        // Do nothing
    }

    @Override
    public void runSimulation(int month) {

        log("[START Month %d (%.02f year)]", month, (month) / 12D);

        List<Person> people = simulation.getPopulation().getPeople();

        for (int i = 0; i < people.size(); i++) {
            Person p = people.get(i);

            simulateEventGettingOlder(p);
            simulateEventDeath(month, p);
            simulateBehaviourMaternity(month, p);
        }

        log("[END Month. Population: %d]", simulation.getPopulation().size());
    }

    @Override
    public void postSimulation(int month) {
        // Build monthly stats
        modelStats.collect(month);

        // Evolve parameters monthly, if needed
        simulation.evolveParametersMonthly(params);
    }

    /*
     * Simulate Events
     */

    protected void simulateEventGettingOlder(Person person) {
        if (person.isAlive()) {
            person.age += 1 / 12D; // One month older
        }
    }

    protected void simulateEventDeath(int month, Person person) {
        // Check probability of death
        if (person.isAlive() && person.age >= person.initialLifeExpectancy) {
            person.deathMonth = month;
            person.status = Person.STATUS.DEAD;

            logDebug("[DEATH] %s - id: %d -age %.2f", person.gender, person.id, person.age);
        }
    }

    protected Person simulateEventBirth(Person mother, double motherAge) {
        Person newBorn = initPerson(null, simulation.getPopulation().size());

        newBorn.mothersAgeAtBirth = motherAge;
        newBorn.age = 0;

        // Update mother's attributes
        mother.numChildren++;
        mother.lastChildAge = motherAge;

        simulation.getPopulation().add(newBorn);

        logDebug("[BIRTH] Mother id: %d age: %02.2f num_children: %d", mother.id, motherAge, mother.numChildren);

        return newBorn;
    }

    protected Person initPerson(Person person, int id, Person.GENDER gender, double age) {

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
        }

        person.initialLifeExpectancy = RandomTools.random(
                params.INITIAL_LIFE_EXPECTANCY_MEAN,
                params.INITIAL_LIFE_EXPECTANCY_STDEV,
                person.age,
                params.INITIAL_LIFE_EXPECTANCY_MAX);

        return person;
    }

    protected Person initPerson(Person person, int id) {
        person = initPerson(person,
                id,
                RandomTools.random(2) == 0 ? Person.GENDER.MALE : Person.GENDER.FEMALE,
                0
        );

        return person;
    }


    /*
     * Simulate behaviours
     */

    protected void simulateBehaviourMaternity(int month, Person person) {

        boolean hasChildAtAge = person.isAlive()
                && person.gender == Person.GENDER.FEMALE
                && person.numChildren < person.initialExpectedChildren
                && person.age >= person.initialFirstChildAge
                && person.age >= params.MATERNITY_MIN_AGE
                && person.age < params.MATERNITY_MAX_AGE;

        if (hasChildAtAge) {
            for (int i = 0; i < person.initialExpectedChildren; i++) {
                Person newBorn = simulateEventBirth(person, person.age);
                // For stats purposes
                newBorn.bornMonth = month;
            }
        }
    }
}