package com.openworldsimulator.demographics;

import com.openworldsimulator.model.Person;
import com.openworldsimulator.simulation.ModelStats;
import com.openworldsimulator.simulation.Simulation;
import com.openworldsimulator.simulation.SimulationModel;
import com.openworldsimulator.tools.RandomTools;

import java.io.File;
import java.util.List;

public class DemographicsModel extends SimulationModel {

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
        return "demographics-model";
    }

    /*
     * Initialization
     *
     */

    @Override
    public void init() {

        super.init();

        modelStats = new DemographicsStats(simulation, params);

        System.out.println("*** INITIALIZING INITIAL POPULATION OF " + params.INITIAL_POPULATION_SIZE);

        if (params.INITIAL_POPULATION_SIZE == 1) {
            // Single person population with ID=1- just for testing
            Person p = new Person();
            simulation.getPopulation().add(
                    initPerson(1, p)
            );
        } else {
            simulation.getLog().setMuted(true);
            // Create a initial amount of people
            for (int i = 0; i < params.INITIAL_POPULATION_SIZE; i++) {
                Person p = new Person();
                simulation.getPopulation().add(
                        initPerson(i, p)
                );
                p.age = params.MATERNITY_MIN_AGE;
                p.age = i % 20;
            }

            // Run runSimulation
            int maxIterations = (int) params.INITIAL_LIFE_EXPECTANCY_MAX * 12 * 2;
            for (int i = 0; i < maxIterations; i++) {
                runSimulation(i);
                System.out.print("*");
                if (i % 120 == 0) {
                    System.out.println(" Init: " + i + " (" + (i / 12) + " years) - Population = " + simulation.getPopulation().getAlivePeople().size());
                }
            }

            modelStats.clearMonthStats();

            // Remove dead people
            simulation.getPopulation().getPeople().removeIf(person -> !person.isAlive());

            // Initialize rest of people
            simulation.getPopulation().getPeople().forEach(
                    p -> {
                        p.bornMonth = -1;
                        p.deathMonth = -1;
                    }
            );
        }

        simulation.log("Starting population: " + simulation.getPopulation().size());

        System.out.println("\n\n*** Starting runSimulation with population of:" + simulation.getPopulation().size());

        simulation.getLog().setMuted(false);
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

        simulation.log("\n[START Month %d (%.02f year)]", month, (month) / 12D);

        List<Person> people = simulation.getPopulation().getPeople();

        for (int i = 0; i < people.size(); i++) {
            Person p = people.get(i);

            simulateEventGettingOlder(p);
            simulateEventDeath(month, p);
            simulateBehaviourMaternity(month, p);
        }

        simulation.log("[END Month. Population: %d]", simulation.getPopulation().size());
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

            simulation.log("  [DEATH] %s - id: %d -age %.2f", person.gender, person.id, person.age);
        }
    }

    protected Person simulateEventBirth(Person mother, double motherAge) {
        Person newBorn = initPerson(simulation.getPopulation().size(), null);

        newBorn.mothersAgeAtBirth = motherAge;
        newBorn.age = 0;

        // Update mother's attributes
        mother.numChildren++;
        mother.lastChildAge = motherAge;

        simulation.getPopulation().add(newBorn);

        simulation.log("  [BIRTH] Mother id: %d age: %02.2f num_children: %d", mother.id, motherAge, mother.numChildren);

        return newBorn;
    }

    protected Person initPerson(int id, Person person) {
        if (person == null) {
            person = new Person();
        }
        person.id = id;
        person.status = Person.STATUS.ALIVE;
        person.gender = RandomTools.random(2) == 0 ? Person.GENDER.MALE : Person.GENDER.FEMALE;
        person.age = 0;

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


    /*
     * Simulate behaviours
     */

    protected void simulateBehaviourMaternity(int month, Person person) {

        boolean hasChildAtAge = person.isAlive()
                && person.gender == Person.GENDER.FEMALE
                && person.numChildren < person.initialExpectedChildren
                && person.age >= person.initialFirstChildAge
                && person.age >= params.MATERNITY_MIN_AGE
                && person.age <  params.MATERNITY_MAX_AGE;

        if (hasChildAtAge) {
            for( int i = 0; i < person.initialExpectedChildren; i++) {
                Person newBorn = simulateEventBirth(person, person.age);
                // For stats purposes
                newBorn.bornMonth = month;
            }
        }
    }
}