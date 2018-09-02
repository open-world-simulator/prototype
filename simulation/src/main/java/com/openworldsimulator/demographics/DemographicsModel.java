package com.openworldsimulator.demographics;

import com.openworldsimulator.model.PopulationSegment;
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

        log("[START Month %d (%.02f year) - Population: %d]", month, (month) / 12D, simulation.getPopulation().size());

        List<PopulationSegment> people = simulation.getPopulation().getPopulationSegments();

        for (int i = 0; i < people.size(); i++) {
            PopulationSegment p = people.get(i);

            simulateEventGettingOlder(p);
            simulateEventDeath(month, p);
            simulateBehaviourMaternity(month, p);
        }

        simulateImmigration(month);
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

    protected void simulateEventGettingOlder(PopulationSegment populationSegment) {
        if (populationSegment.isAlive()) {
            populationSegment.age += 1 / 12D; // One month older
        }
    }

    protected void simulateEventDeath(int month, PopulationSegment populationSegment) {
        // Check probability of death
        if (populationSegment.isAlive() && populationSegment.age >= populationSegment.initialLifeExpectancy) {
            populationSegment.deathMonth = month;
            populationSegment.status = PopulationSegment.STATUS.DEAD;

            logDebug("[DEATH] %s - id: %d -age %.2f", populationSegment.gender, populationSegment.id, populationSegment.age);
        }
    }

    protected PopulationSegment simulateEventBirth(int month, PopulationSegment mother, double motherAge) {
        PopulationSegment newBorn = initPerson(null, simulation.getPopulation().size());

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

    protected void simulateImmigration(int month) {
        if (params.MIGRATION_INFLOW_PCT > 0) {
            int currentPop = simulation.getPopulation().getAlivePeople().size();
            int immigrants = (int) Math.round(currentPop * params.MIGRATION_INFLOW_PCT / (12.0D * 100));

            System.out.printf("[IMMIGRANTS] Immigrants: %d / Population: %d\n", immigrants, currentPop);

            for (int i = 0; i < immigrants; i++) {

                double age = RandomTools.random(
                        params.MIGRATION_INFLOW_AGE_MEAN,
                        params.MIGRATION_INFLOW_AGE_STDEV,
                        0,
                        params.INITIAL_LIFE_EXPECTANCY_MAX
                );

                //System.out.println("Age: " + age);
                PopulationSegment p = initPerson(null, simulation.getPopulation().size(),
                        RandomTools.random(
                                params.MIGRATION_INFLOW_GENDER_DIST
                        ) ? PopulationSegment.GENDER.MALE : PopulationSegment.GENDER.FEMALE,
                        age);

                p.immigrationMonth = month;

                logDebug("[IMMIGRANT] %d age: %02.2f", p.id, p.age);

                simulation.getPopulation().add(p);
            }
        }
    }

    protected PopulationSegment initPerson(PopulationSegment populationSegment, int id, PopulationSegment.GENDER gender, double age) {

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
        }

        populationSegment.initialLifeExpectancy = RandomTools.random(
                params.INITIAL_LIFE_EXPECTANCY_MEAN,
                params.INITIAL_LIFE_EXPECTANCY_STDEV,
                populationSegment.age,
                params.INITIAL_LIFE_EXPECTANCY_MAX);

        return populationSegment;
    }

    protected PopulationSegment initPerson(PopulationSegment populationSegment, int id) {
        populationSegment = initPerson(populationSegment,
                id,
                RandomTools.random(2) == 0 ? PopulationSegment.GENDER.MALE : PopulationSegment.GENDER.FEMALE,
                0
        );

        return populationSegment;
    }


    /*
     * Simulate behaviours
     */

    protected void simulateBehaviourMaternity(int month, PopulationSegment populationSegment) {

        boolean hasChildAtAge = populationSegment.isAlive()
                && populationSegment.gender == PopulationSegment.GENDER.FEMALE
                && populationSegment.numChildren < populationSegment.initialExpectedChildren
                && populationSegment.age >= populationSegment.initialFirstChildAge
                && populationSegment.age >= params.MATERNITY_MIN_AGE
                && populationSegment.age < params.MATERNITY_MAX_AGE;

        if (hasChildAtAge) {
            for (int i = 0; i < populationSegment.initialExpectedChildren; i++) {
                simulateEventBirth(month, populationSegment, populationSegment.age);
            }
        }
    }
}