package com.openworldsimulator.economics;

import com.openworldsimulator.model.Person;
import com.openworldsimulator.simulation.ModelStats;
import com.openworldsimulator.simulation.Simulation;
import com.openworldsimulator.simulation.SimulationModel;
import com.openworldsimulator.tools.RandomTools;

import java.io.File;
import java.util.List;

public class EconomyModel extends SimulationModel {

    private EconomyParams params;
    private MicroEconomyStats modelStats;
    private MacroEconomyStats macroStats;

    public EconomyModel(Simulation simulation, File outputPath, EconomyParams params) {
        super(outputPath, simulation);
        this.params = params;
    }

    @Override
    public String getId() {
        return "economics-model";
    }

    @Override
    public ModelStats getStats() {
        return modelStats;
    }

    @Override
    public void init() {
        super.init();

        simulation.log("* Initializing global balance sheets");
        simulation.getPublicSector().getBalanceSheet().reset();
        simulation.getCompanies().getBalanceSheet().reset();

        simulation.log("* Initializing population income model.");
        simulation.log(params.toString());

        simulation.getPopulation().getPeople().forEach(
                this::initEconomicData
        );

        modelStats = new MicroEconomyStats(params, simulation);
        macroStats = new MacroEconomyStats(simulation);
    }


    void initEconomicData(Person p) {
        p.initialFirstJobAge = RandomTools.random(params.FIRST_JOB_AGE_MEAN, params.FIRST_JOB_AGE_STDEV);
        p.retirementAge = RandomTools.random(params.RETIRE_AGE_MEAN, params.RETIRE_AGE_STDEV);
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
        simulation.log("\n[ECONOMICS]", month, (month) / 12D);

        List<Person> people = simulation.getPopulation().getPeople();

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
            simulateRetirementIncome(month, p);
            simulateJobIncome(month, p);
            simulateSavingsYieldIncome(month, p);

            // Simulate expenses
            simulateNonDiscretionaryExpenses(month, p);
            simulateDiscretionaryExpenses(month, p);

            // Simulate consumption debt
            simulateConsumptionDebt(month, p);

            // Simulate saving & investment decisions
            //   simulateInvestmentDecisions(month, p);
        }

        simulation.log("[END Economics %d]", simulation.getPopulation().size());
    }

    @Override
    public void postSimulation(int month) {
        // Build monthly stats
        modelStats.collect(month);

        simulatePublicSectorDebtService(month);

        // Build monthly stats
        macroStats.collect(month);

        // Evolve parameters monthly, if needed
        simulation.evolveParametersMonthly(params);
    }

    void simulateConsumptionDebt(int month, Person p) {

    }

    void simulateInvestmentDecisions(int month, Person p) {
        // TODO
    }


    void simulateEmploymentStatus(int month, Person person) {
        if (!person.isAlive()) return;
        if (person.age >= person.initialFirstJobAge && person.monthlyData.monthIncomeWage == 0) {
            // Find a job
            if( RandomTools.random(params.JOB_MARKET_PUBLIC_SECTOR_RATE) ) {
                person.economicStatus = Person.ECONOMIC_STATUS.WORKING_PUBLIC_SECTOR;
                person.grossMonthlySalary = RandomTools.random(params.PUBLIC_SECTOR_MONTHLY_WAGE_MEAN, params.PUBLIC_SECTOR_MONTHLY_WAGE_STDEV);
            } else {
                person.economicStatus = Person.ECONOMIC_STATUS.WORKING_PRIVATE_SECTOR;
                person.grossMonthlySalary = RandomTools.random(params.PRIVATE_SECTOR_MONTHLY_WAGE_MEAN, params.PRIVATE_SECTOR_MONTHLY_WAGE_STDEV);
            }
            if (person.grossMonthlySalary < params.MINIMAL_WAGE) {
                person.grossMonthlySalary = params.MINIMAL_WAGE;
            }
            simulation.log("[JOBS] Person " + person.id + " finds job of %.2f at age %.2f", person.grossMonthlySalary, person.age);
        }
    }

    void simulateJobIncome(int month, Person person) {
        if (!person.isAlive()) return;

        // TODO: Unemployment
        if (person.age >= person.initialFirstJobAge && person.age < person.retirementAge) {
            person.monthlyData.monthIncomeWage = person.grossMonthlySalary;

            double employeeTaxes = person.monthlyData.monthIncomeWage  * params.TAX_ON_INCOME_EMPLOYEE_RATE;
            double employerTaxes = person.monthlyData.monthIncomeWage  * params.TAX_ON_INCOME_EMPLOYER_RATE;

            if( person.economicStatus.equals(Person.ECONOMIC_STATUS.WORKING_PRIVATE_SECTOR)) {
                simulation.getTransactions().earnIncomeFromCompanies(person, person.monthlyData.monthIncomeWage);
                simulation.getTransactions().payTaxes(person, employeeTaxes);
                simulation.getTransactions().payTaxes(simulation.getCompanies(), employerTaxes);
            } else {
                // Public sector worker
                simulation.getTransactions().earnIncomeFromGovernment(person, person.monthlyData.monthIncomeWage);
                simulation.getTransactions().payTaxes(person, employeeTaxes);
            }

            person.monthlyData.monthTaxesIncome += employeeTaxes;
        }
    }

    void simulateRetirementIncome(int month, Person person) {
        if (!person.isAlive()) return;

        if (person.age >= person.retirementAge && person.monthlyData.monthIncomePension == 0) {
            person.grossPension = person.monthlyData.monthIncomeWage * params.PENSION_REPLACEMENT_RATE;
            person.monthlyData.monthIncomeWage = 0;
            person.economicStatus = Person.ECONOMIC_STATUS.RETIRED;
            simulation.log("Person " + person.id + " starts receiving avgMonthlyIncomePension of %.2f at age %.2f", person.monthlyData.monthIncomePension, person.age);
        }

        person.monthlyData.monthIncomePension  = person.grossPension;

        if( person.monthlyData.monthIncomePension == 0) {
            return;
        }

        double taxes = person.monthlyData.monthIncomePension * params.TAX_ON_INCOME_EMPLOYEE_RATE;

        simulation.getTransactions().earnIncomeFromGovernment(person, person.monthlyData.monthIncomePension);
        simulation.getTransactions().payTaxes(person, taxes);

        person.monthlyData.monthTaxesIncome += taxes;
    }

    void simulateEconomicDeath(int month, Person person) {

//        simulateInheritance(month, person);

        // Remove sources of income
        person.monthlyData.reset();

        person.getBalanceSheet().reset();

        person.economicStatus = Person.ECONOMIC_STATUS.NONE;

        simulation.log("Person " + person.id + " economic death at age %.2f", person.age);
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

            simulation.log("[INHERITANCE] Person %s received %.2f", receiver.toString(),
                    receiver.getBalanceSheet().getSavings()
            );
        } else {
            simulation.log("[ERROR] No alive people left to receive inheritance");
        }
    }

    void simulateSavingsYieldIncome(int month, Person person) {

        person.monthlyData.monthIncomeSavings = person.getBalanceSheet().getSavings() * params.YIELD_SAVINGS_RATE / 12D;

        double taxes = person.monthlyData.monthIncomeSavings * params.TAX_ON_SAVINGS_PCT;

        // Cash in interests
        simulation.getTransactions().earnIncomeFromBanks(person, person.monthlyData.monthIncomeSavings);
        simulation.getTransactions().payTaxes(person, taxes);

        person.monthlyData.monthTaxesFinancial += taxes;
    }


    void simulateNonDiscretionaryExpenses(int month, Person person) {
        if (!person.isAlive()) return;

        // TODO: Simulate based on income and situation (i.e age)
        person.monthlyData.monthExpensesNonDiscretionary = RandomTools.random(
                params.MONTHLY_NON_DISCRETIONARY_EXPENSES_MEAN,
                params.MONTHLY_NON_DISCRETIONARY_EXPENSES_STDEV,
                params.MONTHLY_NON_DISCRETIONARY_EXPENSES_MIN,
                -1
        );

        if (person.monthlyData.monthExpensesNonDiscretionary > person.getBalanceSheet().getSavings()) {
            simulation.log("[EXPENSES1] Person %d - Savings: %.2f Expenses: %.2f", person.id, person.getBalanceSheet().getSavings(), person.monthlyData.monthExpensesNonDiscretionary);

            // TODO: Simulate debt or asset liquidation
            // person.monthExpensesNonDiscretionary = person.getSavings();
        }

        double taxes = person.getMonthlyData().monthExpensesNonDiscretionary * params.TAX_ON_NON_DISCRETIONARY_CONSUMPTION_RATE;

        simulation.getTransactions().expend(person, person.monthlyData.monthExpensesNonDiscretionary);
        simulation.getTransactions().payTaxes(person, taxes);

        person.getMonthlyData().monthExpensesNonDiscretionary += taxes;

        // Assume no debt yet
    }

    void simulateDiscretionaryExpenses(int month, Person person) {
        if (!person.isAlive()) return;

        double remainingIncome = person.monthlyData.getTotalMonthIncome() - person.monthlyData.getTotalExpenses() - person.monthlyData.getTotalTaxes();
        if (remainingIncome < 0) {
            // Income < non discretionary expenses => don't expend additional
            remainingIncome = 0;
        }

        person.monthlyData.monthExpensesDiscretionary = RandomTools.random(
                params.PROPENSITY_TO_CONSUMPTION_MEAN_RATE,
                params.PROPENSITY_TO_CONSUMPTION_STDEV_RATE
        ) * (1 - params.TAX_ON_DISCRETIONARY_CONSUMPTION_RATE) * remainingIncome;

        if (person.monthlyData.monthExpensesDiscretionary > person.getBalanceSheet().getSavings()) {
            simulation.log("[EXPENSES2] Person %d - Savings: %.2f Expenses: %.2f", person.id, person.getBalanceSheet().getSavings(), person.monthlyData.monthExpensesDiscretionary);

            // TODO: Simulate debt or asset liquidation
            //person.monthExpensesDiscretionary = person.getSavings();
        }

        double taxes = person.getMonthlyData().monthExpensesDiscretionary * params.TAX_ON_DISCRETIONARY_CONSUMPTION_RATE;

        simulation.getTransactions().expend(person, person.monthlyData.monthExpensesDiscretionary);
        simulation.getTransactions().payTaxes(person, taxes);

        person.getMonthlyData().monthExpensesDiscretionary += taxes;
    }

    void simulatePublicSectorDebtService(int month) {
        // TODO: Add cost of debt for public government
        double publicDebt = simulation.getPublicSector().getBalanceSheet().getDebt();
        double costOfDebt = publicDebt * params.AVERAGE_COST_OF_PUBLIC_DEBT / 12.0D;

        simulation.getPublicSector().getBalanceSheet().decreaseSavings(costOfDebt);
        simulation.getPublicSector().getMonthlyResults().addExpenses(costOfDebt);
        // TODO: Government debt is in the hand of either population or companies

        System.out.println("PUBLIC DEBT: " + publicDebt + " COST OF DEBT: " + costOfDebt);
    }
}
