package com.openworldsimulator.economics;

import com.openworldsimulator.economics.stats.MacroBalanceSheetStats;
import com.openworldsimulator.economics.stats.CompaniesStats;
import com.openworldsimulator.economics.stats.GovernmentStats;
import com.openworldsimulator.economics.stats.PersonalEconomyStats;
import com.openworldsimulator.model.Person;
import com.openworldsimulator.simulation.ModelParameters;
import com.openworldsimulator.simulation.ModelStats;
import com.openworldsimulator.simulation.Simulation;
import com.openworldsimulator.simulation.SimulationModel;
import com.openworldsimulator.tools.RandomTools;

import java.io.File;
import java.util.List;

public class EconomyModel extends SimulationModel {

    public static final String MODEL_ID = "economics-model";
    private EconomyParams params;
    private ModelStats[] allStats;

    public EconomyModel(Simulation simulation, File outputPath, EconomyParams params) {
        super(outputPath, simulation);
        this.params = params;
    }

    @Override
    public String getId() {
        return MODEL_ID;
    }

    @Override
    public ModelStats[] getStats() {

        if (!isEnabled()) {
            return null;
        }

        return allStats;
    }

    @Override
    public ModelParameters getParams() {
        return params;
    }

    private boolean isEnabled() {
        return params._ENABLE_ECONOMY_SIMULATION != 0;
    }

    @Override
    public void init() {
        super.init();

        if (!isEnabled()) {
            return;
        }

        log("* Initializing global balance sheets");
        simulation.getPublicSector().getBalanceSheet().reset();
        simulation.getCompanies().getBalanceSheet().reset();

        log("* Initializing population income model.");
        log(params.toString());

        simulation.getPopulation().getPopulationSegments().forEach(
                this::initEconomicData
        );

        allStats = new ModelStats[]{
                new PersonalEconomyStats(simulation),
                new GovernmentStats(simulation),
                new CompaniesStats(simulation),
                new MacroBalanceSheetStats(simulation)
        };
    }


    void initEconomicData(Person p) {
        p.initialFirstJobAge = RandomTools.random(params.jobMarket.FIRST_JOB_AGE_MEAN, params.jobMarket.FIRST_JOB_AGE_STDEV);
        p.retirementAge = RandomTools.random(params.jobMarket.RETIRE_AGE_MEAN, params.jobMarket.RETIRE_AGE_STDEV);
        p.economicStatus = Person.ECONOMIC_STATUS.NONE;
        p.getMonthlyData().reset();
    }

    @Override
    public void preSimulation(int month) {
        simulation.getPublicSector().getMonthlyResults().reset();
        simulation.getCompanies().getMonthlyResults().reset();
        simulation.getBanks().getMonthlyResults().reset();
    }

    @Override
    public void runSimulation(int month) {
        if (!isEnabled()) {
            return;
        }

        log("\n[ECONOMICS]", month, (month) / 12D);

        List<Person> people = simulation.getPopulation().getPopulationSegments();

        for (Person p : people) {
            // Init cycle vars
            p.getMonthlyData().reset();

            if (p.justDead(month)) {
                // Deal with person dead
                simulateEconomicDeath(month, p);
            }

            if (p.justBorn(month)) {
                initEconomicData(p);
            }

            // Simulate job & lifecycle events
            simulateEmploymentStatus(month, p);

            // Collect income
            simulateJobIncome(month, p);
            simulateRetirementIncome(month, p);
     //       simulateSavingsYieldIncome(month, p);

            // Simulate expenses
            simulateNonDiscretionaryExpenses(month, p);
            simulateDiscretionaryExpenses(month, p);

            // Simulate consumption debt
       ///     simulateConsumptionDebt(month, p);

            // Simulate saving & investment decisions
            //   simulateInvestmentDecisions(month, p);
        }

        log("[END Economics %d]", simulation.getPopulation().size());
    }

    @Override
    public void postSimulation(int month) {
        if (!isEnabled()) {
            return;
        }

        //simulatePublicSectorDebtService(month);

        for (ModelStats s : allStats) {
            s.collect(month);
        }

        // Evolve parameters monthly, if needed
        simulation.evolveParametersMonthly(params);
    }

    void simulateConsumptionDebt(int month, Person p) {
        //  TODO
    }

    void simulateInvestmentDecisions(int month, Person p) {
        // TODO
    }


    void simulateEmploymentStatus(int month, Person person) {
        if (!person.isAlive()) return;
        if (person.age >= person.initialFirstJobAge && person.monthlyData.incomeWage == 0) {
            // Find a job
            if (RandomTools.random(params.jobMarket.PUBLIC_SECTOR_WORK_PCT)) {
                // Finds a job in public sector
                person.economicStatus = Person.ECONOMIC_STATUS.WORKING_PUBLIC_SECTOR;
                person.grossMonthlySalary = RandomTools.random(params.jobMarket.PUBLIC_SECTOR_YEARLY_WAGE_MEAN, params.jobMarket.PUBLIC_SECTOR_YEARLY_WAGE_STDEV);
            } else {
                // Finds a job in private sector
                person.economicStatus = Person.ECONOMIC_STATUS.WORKING_PRIVATE_SECTOR;
                person.grossMonthlySalary = RandomTools.random(params.jobMarket.PRIVATE_SECTOR_YEARLY_WAGE_MEAN, params.jobMarket.PRIVATE_SECTOR_YEARLY_WAGE_STDEV);
            }
            if (person.grossMonthlySalary < params.jobMarket.MINIMAL_WAGE) {
                person.grossMonthlySalary = params.jobMarket.MINIMAL_WAGE;
            }
            log("[JOBS] Person " + person.id + " finds job of %.2f at age %.2f", person.grossMonthlySalary, person.age);
        }
    }

    void simulateJobIncome(int month, Person person) {
        if (!person.isAlive()) return;

        // TODO: Unemployment
        if (person.age >= person.initialFirstJobAge && person.age < person.retirementAge) {
            person.monthlyData.incomeWage = person.grossMonthlySalary;

            double employeeTaxes = person.monthlyData.incomeWage * params.government.TAX_ON_INCOME_EMPLOYEE_RATE;
            double employerTaxes = person.monthlyData.incomeWage * params.government.TAX_ON_INCOME_EMPLOYER_RATE;

            if (person.economicStatus.equals(Person.ECONOMIC_STATUS.WORKING_PRIVATE_SECTOR)) {
                simulation.getTransactions().earnIncomeFromCompanies(person, person.monthlyData.incomeWage);
                simulation.getTransactions().payTaxes(person, employeeTaxes);
                simulation.getTransactions().payTaxes(simulation.getCompanies(), employerTaxes);
            } else {
                // Public sector worker
                simulation.getTransactions().earnIncomeFromGovernment(person, person.monthlyData.incomeWage);
                simulation.getTransactions().payTaxes(person, employeeTaxes);
            }

            person.monthlyData.taxesIncome += employeeTaxes;
        }
    }

    void simulateRetirementIncome(int month, Person person) {
        if (!person.isAlive()) return;
        if(person.age < person.retirementAge ) return;

        if (person.grossPension == 0) {
            person.grossPension = person.grossMonthlySalary * params.government.PENSION_REPLACEMENT_RATE;
            logDebug("Person " + person.id + " starts receiving avgMonthlyIncomePension of %.2f at age %.2f", person.grossPension, person.age);
        }

        person.monthlyData.incomeWage = 0;
        person.monthlyData.incomePension = person.grossPension;
        person.economicStatus = Person.ECONOMIC_STATUS.RETIRED;

        if (person.monthlyData.incomePension == 0) {
            return;
        }

        double taxes = person.monthlyData.incomePension * params.government.TAX_ON_INCOME_EMPLOYEE_RATE;

        simulation.getTransactions().earnIncomeFromGovernment(person, person.monthlyData.incomePension);
        simulation.getTransactions().payTaxes(person, taxes);

        person.monthlyData.taxesIncome += taxes;
    }

    void simulateEconomicDeath(int month, Person person) {

//        simulateInheritance(month, person);

        // Remove sources of income
        person.monthlyData.reset();

        person.getBalanceSheet().reset();

        person.economicStatus = Person.ECONOMIC_STATUS.NONE;

        log("Person " + person.id + " economic death at age %.2f", person.age);
    }

    void simulateInheritance(int month, Person person) {
        // Assign all assets to one random living person
        List<Person> alive = simulation.getPopulation().getAlivePeople();
        if (alive.size() > 0) {
            Person receiver = alive.get(RandomTools.random(alive.size()));

            simulation.getTransactions().pay(
                    person.getBalanceSheet().getSavings(),
                    person,
                    receiver
            );

            log("[INHERITANCE] Person %s received %.2f", receiver.toString(),
                    receiver.getBalanceSheet().getSavings()
            );
        } else {
            log("[ERROR] No alive people left to receive inheritance");
        }
    }

    void simulateSavingsYieldIncome(int month, Person person) {

        person.monthlyData.incomeSavings = person.getBalanceSheet().getSavings() * params.personalEconomy.YIELD_SAVINGS_RATE / 12D;

        double taxes = person.monthlyData.incomeSavings * params.government.TAX_ON_SAVINGS_PCT;

        // Cash in interests
        simulation.getTransactions().earnIncomeFromBanks(person, person.monthlyData.incomeSavings);
        simulation.getTransactions().payTaxes(person, taxes);

        person.monthlyData.taxesFinancial += taxes;
    }


    void simulateNonDiscretionaryExpenses(int month, Person person) {
        if (!person.isAlive()) return;

        // TODO: Simulate based on income and situation (i.e age)
        person.monthlyData.consumptionNonDiscretionary = RandomTools.random(
                params.personalEconomy.MONTHLY_NON_DISCRETIONARY_EXPENSES_MEAN,
                params.personalEconomy.MONTHLY_NON_DISCRETIONARY_EXPENSES_STDEV,
                params.personalEconomy.MONTHLY_NON_DISCRETIONARY_EXPENSES_MIN,
                -1
        );

        if (person.monthlyData.consumptionNonDiscretionary > person.getBalanceSheet().getSavings()) {
            log("[EXPENSES1] Person %d - Savings: %.2f Expenses: %.2f", person.id, person.getBalanceSheet().getSavings(), person.monthlyData.consumptionNonDiscretionary);

            // TODO: Simulate debt or asset liquidation
            // person.consumptionNonDiscretionary = person.getSavings();
        }

        double taxes = person.getMonthlyData().consumptionNonDiscretionary * params.government.TAX_ON_NON_DISCRETIONARY_CONSUMPTION_RATE;

        simulation.getTransactions().expend(person, person.monthlyData.consumptionNonDiscretionary);
        simulation.getTransactions().payTaxes(person, taxes);

        person.getMonthlyData().consumptionNonDiscretionary += taxes;

        // Assume no debt yet
    }

    void simulateDiscretionaryExpenses(int month, Person person) {
        if (!person.isAlive()) return;

        double remainingIncome = person.monthlyData.getTotalMonthIncome() - person.monthlyData.getTotalConsumption() - person.monthlyData.getTotalTaxes();
        if (remainingIncome < 0) {
            // Income < non discretionary expenses => don't expend additional
            remainingIncome = 0;
        }

        person.monthlyData.consumptionDiscretionary = RandomTools.random(
                params.personalEconomy.PROPENSITY_TO_CONSUMPTION_MEAN_RATE,
                params.personalEconomy.PROPENSITY_TO_CONSUMPTION_STDEV_RATE
        ) * (1 - params.government.TAX_ON_DISCRETIONARY_CONSUMPTION_RATE) * remainingIncome;

        if (person.monthlyData.consumptionDiscretionary > person.getBalanceSheet().getSavings()) {
            log("[EXPENSES2] Person %d - Savings: %.2f Expenses: %.2f", person.id, person.getBalanceSheet().getSavings(), person.monthlyData.consumptionDiscretionary);

            // TODO: Simulate debt or asset liquidation
            //person.consumptionDiscretionary = person.getSavings();
        }

        double taxes = person.getMonthlyData().consumptionDiscretionary * params.government.TAX_ON_DISCRETIONARY_CONSUMPTION_RATE;

        simulation.getTransactions().expend(person, person.monthlyData.consumptionDiscretionary);
        simulation.getTransactions().payTaxes(person, taxes);

        person.getMonthlyData().consumptionDiscretionary += taxes;
    }
/*
    void simulatePublicSectorDebtService(int month) {
        // TODO: Add cost of debt for public government
        double publicDebt = simulation.getPublicSector().getBalanceSheet().getDebt();
        double costOfDebt = publicDebt * params.government.AVERAGE_COST_OF_PUBLIC_DEBT / 12.0D;

        simulation.getPublicSector().getBalanceSheet().decreaseSavings(costOfDebt);
        simulation.getPublicSector().getMonthlyResults().addExpenses(costOfDebt);
        // TODO: Government debt is in the hand of either population or companies

        System.out.println("PUBLIC DEBT: " + publicDebt + " COST OF DEBT: " + costOfDebt);
    }
*/
}
