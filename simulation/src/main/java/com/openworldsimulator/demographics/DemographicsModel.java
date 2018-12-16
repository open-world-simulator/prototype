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

public class DemographicsModel extends SimulationModel {

    public static final String MODEL_ID = "demographics-model";

    private DemographicParams params;

    /**
     * Stats collection
     **/
    private DemographicsStats modelStats;

    private ModelStats[] allStats = null;

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
        allStats = new ModelStats[]{modelStats};

        log("*** INITIALIZING INITIAL POPULATION OF " + params.INITIAL_POPULATION_SIZE);

        InitialPopulationLoader loader = new InitialPopulationLoader(
                simulation.getPopulation(),
                params,
                params.INITIAL_DEMOGRAPHY_DATA_COUNTRY,
                simulation.getBaseYear(),
                (int) params.INITIAL_POPULATION_SIZE
        );

        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            throw new ExceptionInInitializerError(e);
        }

        log("Starting runSimulation with population of:" + simulation.getPopulation().size());
    }

    @Override
    public ModelStats[] getStats() {
        return allStats;
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

        log("[START Month %d (%.02f year) - Population: %d]", month, (month) / 12D, simulation.getPopulation().size());

        parallelRun(
                simulation.getPopulation().getPopulationSegments(),
                populationSegment -> {
                    for (Person person : populationSegment) {
                        simulateEventGettingOlder(person);
                        simulateEventDeath(month, person);
                        simulateBehaviourMaternity(month, person);
                        simulateEmigration(month, person);
                    }
                }
        );

        simulateImmigration(month);
    }

    @Override
    public void postSimulation(int month) {
        // Build monthly stats
        modelStats.collect(month);
    }

    /*
     * Simulate Events
     */

    protected void simulateEventGettingOlder(Person person) {
        if (person.isInPopulation()) {
            person.age += 1 / 12D; // One month older
        }
    }

    protected void simulateEventDeath(int month, Person person) {
        // Check probability of death
        if (person.isInPopulation() && person.age >= person.initialLifeExpectancy) {
            person.deathMonth = month;
            person.status = Person.LIFE_STATUS.DEAD;

            logDebug("[DEATH] %s - id: %d -age %.2f", person.gender, person.id, person.age);
        }
    }

    protected Person simulateEventBirth(int month, Person mother, double motherAge) {
        Person newBorn = initPerson(null, simulation.getPopulation().size());

        newBorn.mothersAgeAtBirth = motherAge;
        newBorn.age = 0;
        newBorn.bornMonth = month;

        // Update mother's attributes
        mother.numChildren++;
        mother.lastChildAge = motherAge;

        simulation.getPopulation().add(newBorn);

        logDebug("[BIRTH] Mother id: %d age: %02.2f num_children: %d", mother.id, motherAge, mother.numChildren);

        return newBorn;
    }

    protected void simulateEmigration(int month, Person person) {

        if (person.isInPopulation()) {
            double halfMonth = 0.5 / 12.0;
            if (RandomTools.testNormalDist(
                    person.age - halfMonth, person.age + halfMonth,
                    params.MIGRATION_OUTFLOW_AGE_MEAN,
                    params.MIGRATION_OUTFLOW_AGE_STDEV,
                    params.MIGRATION_OUTFLOW_BASE_PCT / 100.0
            )) {
                person.status = Person.LIFE_STATUS.GONE;
                person.emigrationMonth = month;

                // Emigrate
                log("[EMIGRANT] %d age: %02.2f", person.id, person.age);
            }
        }
    }

    protected void simulateImmigration(int month) {
        if (params.MIGRATION_INFLOW_BASE_PCT > 0) {
            int currentPop = simulation.getPopulation().getPeopleInPopulation().size();
            int immigrants = 0;

            immigrants += params.INITIAL_POPULATION_SIZE * params.MIGRATION_INFLOW_BASE_PCT / (12.0D * 100);
            //immigrants += (int) Math.round(currentPop * params.MIGRATION_INFLOW_PCT / (12.0D * 100));

            logDebug("[IMMIGRANTS] Immigrants: %d / Population: %d\n", immigrants, currentPop);

            for (int i = 0; i < immigrants; i++) {

                double age = RandomTools.random(
                        params.MIGRATION_INFLOW_AGE_MEAN,
                        params.MIGRATION_INFLOW_AGE_STDEV,
                        0,
                        params.INITIAL_LIFE_EXPECTANCY_MAX,
                        "Immigrant Age"
                );

                //System.out.println("Age: " + age);
                Person p = initPerson(null, simulation.getPopulation().size(),
                        RandomTools.testUniformDist(
                                params.MIGRATION_INFLOW_GENDER_DIST
                        ) ? Person.GENDER.MALE : Person.GENDER.FEMALE,
                        age);

                p.immigrationMonth = month;
                p.immigrationAge = p.age;

                logDebug("[IMMIGRANT] %d age: %02.2f", p.id, p.age);

                simulation.getPopulation().add(p);
            }
        }
    }

    protected Person initPerson(Person person, int id, Person.GENDER gender, double age) {

        if (person == null) {
            person = new Person();
        }
        person.id = id;
        person.gender = gender;
        person.status = Person.LIFE_STATUS.ALIVE;

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
                            params.MATERNITY_MAX_AGE,
                            "Initial First Child Age");

            person.initialExpectedChildren =
                    (int) Math.abs(RandomTools.random(
                            params.MATERNITY_NUM_CHILDREN_MEAN,
                            params.MATERNITY_NUM_CHILDREN_STDEV));
        }

        person.initialLifeExpectancy = RandomTools.random(
                params.INITIAL_LIFE_EXPECTANCY_MEAN,
                params.INITIAL_LIFE_EXPECTANCY_STDEV,
                person.age,
                params.INITIAL_LIFE_EXPECTANCY_MAX,
                "Initial Life Expectancy");

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

        boolean hasChildAtAge = person.isInPopulation()
                && person.gender == Person.GENDER.FEMALE
                && person.numChildren < person.initialExpectedChildren
                && person.age >= person.initialFirstChildAge
                && person.age >= params.MATERNITY_MIN_AGE
                && person.age < params.MATERNITY_MAX_AGE;

        if (hasChildAtAge) {
            for (int i = 0; i < person.initialExpectedChildren; i++) {
                simulateEventBirth(month, person, person.age);
            }
        }
    }
}