package com.openworldsimulator.economics;

import com.openworldsimulator.economics.stats.CompaniesStats;
import com.openworldsimulator.economics.stats.GovernmentStats;
import com.openworldsimulator.economics.stats.HouseholdEconomyStats;
import com.openworldsimulator.economics.stats.MacroBalanceSheetStats;
import com.openworldsimulator.model.Government;
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
        simulation.getGovernment().getBalanceSheet().reset();
        simulation.getCompanies().getBalanceSheet().reset();

        log("* Initializing population income model.");
        log(params.toString());

        simulation.getPopulation().getPopulationSegments().forEach(
                this::initEconomicData
        );

        allStats = new ModelStats[]{
                new HouseholdEconomyStats(simulation),
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
        simulation.getGovernment().getMonthlyResults().reset();
        simulation.getCompanies().getMonthlyResults().reset();
        simulation.getBanks().getMonthlyResults().reset();
    }

    @Override
    public void runSimulation(int month) {
        if (!isEnabled()) {
            return;
        }

        logDebug("\n - [ECONOMICS]", month, (month) / 12D);

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
            simulateSavingsYieldIncome(month, p);

            // Simulate expenses
            simulateNonDiscretionaryExpenses(month, p);
            simulateDiscretionaryExpenses(month, p);

            // Simulate consumption debt
            ///     simulateConsumptionDebt(month, p);

            // Simulate saving & investment decisions
            //   simulateInvestmentDecisions(month, p);
        }

        simulateGovernmentFinancing(month);

        logDebug("[END Economics %d]", simulation.getPopulation().size());
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
                person.grossMonthlySalary = RandomTools.random(
                        params.jobMarket.PUBLIC_SECTOR_YEARLY_WAGE_MEAN / 12.0,
                        params.jobMarket.PUBLIC_SECTOR_YEARLY_WAGE_STDEV / 12.0);
            } else {
                // Finds a job in private sector
                person.economicStatus = Person.ECONOMIC_STATUS.WORKING_PRIVATE_SECTOR;
                person.grossMonthlySalary = RandomTools.random(params.jobMarket.PRIVATE_SECTOR_YEARLY_WAGE_MEAN / 12.0, params.jobMarket.PRIVATE_SECTOR_YEARLY_WAGE_STDEV / 12.0);
            }
            if (person.grossMonthlySalary < params.jobMarket.MINIMAL_MONTHLY_WAGE) {
                person.grossMonthlySalary = params.jobMarket.MINIMAL_MONTHLY_WAGE;
            }
            logDebug("[JOBS] Person " + person.id + " finds job of %.2f at age %.2f", person.grossMonthlySalary, person.age);
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
                simulation.getTransactions().payTaxes(person, employeeTaxes, Government.TYPE_INCOME_TAXES_INCOME);
                simulation.getTransactions().payTaxes(simulation.getCompanies(), employerTaxes, Government.TYPE_INCOME_TAXES_INCOME);
            } else {
                // Public sector worker
                simulation.getTransactions().earnIncomeFromGovernment(person, person.monthlyData.incomeWage, Government.TYPE_EXPENSES_EMPLOYMENT);
                simulation.getTransactions().payTaxes(person, employeeTaxes, Government.TYPE_INCOME_TAXES_INCOME);
            }

            person.monthlyData.taxesIncome += employeeTaxes;
        }
    }

    void simulateRetirementIncome(int month, Person person) {
        if (!person.isAlive()) return;
        if (person.age < person.retirementAge) return;

        if (person.grossPension == 0) {
            person.grossPension = person.grossMonthlySalary * params.government.PENSION_REPLACEMENT_RATE;
            logDebug("Person " + person.id + " starts receiving pension of %.2f at age %.2f", person.grossPension, person.age);
        }

        person.monthlyData.incomeWage = 0;
        person.monthlyData.incomePension = person.grossPension;
        person.economicStatus = Person.ECONOMIC_STATUS.RETIRED;

        if (person.monthlyData.incomePension == 0) {
            return;
        }

        double taxes = person.monthlyData.incomePension * params.government.TAX_ON_INCOME_EMPLOYEE_RATE;

        simulation.getTransactions().earnIncomeFromGovernment(person, person.monthlyData.incomePension, Government.TYPE_EXPENSES_PENSIONS);
        simulation.getTransactions().payTaxes(person, taxes, Government.TYPE_INCOME_TAXES_INCOME);

        person.monthlyData.taxesIncome += taxes;
    }

    public static final String TYPE_INCOME_TAXES = "TYPE_TAXES";

    void simulateEconomicDeath(int month, Person person) {

//        simulateInheritance(month, person);

        // Remove sources of income
        person.monthlyData.reset();

        person.getBalanceSheet().reset();

        person.economicStatus = Person.ECONOMIC_STATUS.NONE;

        logDebug("Person " + person.id + " economic death at age %.2f", person.age);
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

            logDebug("[INHERITANCE] Person %s received %.2f", receiver.toString(),
                    receiver.getBalanceSheet().getSavings()
            );
        } else {
            logDebug("[ERROR] No alive people left to receive inheritance");
        }
    }

    void simulateSavingsYieldIncome(int month, Person person) {

        person.monthlyData.incomeSavings = person.getBalanceSheet().getSavings() * params.personalEconomy.YIELD_SAVINGS_RATE / 12D;

        double taxes = person.monthlyData.incomeSavings * params.government.TAX_ON_SAVINGS_PCT;

        // Cash in interests
        simulation.getTransactions().earnIncomeFromBanks(person, person.monthlyData.incomeSavings);
        simulation.getTransactions().payTaxes(person, taxes, Government.TYPE_INCOME_TAXES_SAVINGS);

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
            logDebug("[EXPENSES1] Person %d - Savings: %.2f Expenses: %.2f", person.id, person.getBalanceSheet().getSavings(), person.monthlyData.consumptionNonDiscretionary);

            // TODO: Simulate debt or asset liquidation
            // person.consumptionNonDiscretionary = person.getSavings();
        }

        double taxes = person.getMonthlyData().consumptionNonDiscretionary * params.government.TAX_ON_NON_DISCRETIONARY_CONSUMPTION_RATE;

        simulation.getTransactions().expend(person, person.monthlyData.consumptionNonDiscretionary);
        simulation.getTransactions().payTaxes(person, taxes, Government.TYPE_INCOME_TAXES_CONSUMPTION);

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
            logDebug("[EXPENSES2] Person %d - Savings: %.2f Expenses: %.2f", person.id, person.getBalanceSheet().getSavings(), person.monthlyData.consumptionDiscretionary);

            // TODO: Simulate debt or asset liquidation
            //person.consumptionDiscretionary = person.getSavings();
        }

        double taxes = person.getMonthlyData().consumptionDiscretionary * params.government.TAX_ON_DISCRETIONARY_CONSUMPTION_RATE;

        simulation.getTransactions().expend(person, person.monthlyData.consumptionDiscretionary);
        simulation.getTransactions().payTaxes(person, taxes, Government.TYPE_INCOME_TAXES_CONSUMPTION);

        person.getMonthlyData().consumptionDiscretionary += taxes;
    }

    void simulateGovernmentFinancing(int month) {

        // First, pay interest on existing debt stock
        double debtExpenses = simulation.getGovernment().getBalanceSheet().getDebt() * params.government.DEBT_INTEREST_RATE / 12.0;
        simulation.getGovernment().getBalanceSheet().decreaseSavings(debtExpenses);
        simulation.getGovernment().getMonthlyResults().addExpenses(debtExpenses, Government.TYPE_EXPENSES_DEBT);

        // Create new debt to finance deficit
        double deficit = simulation.getGovernment().getMonthlyResults().getMonthlyResult() - debtExpenses;

        if (deficit < 0) {
            logDebug("Government financing needs %.2f", deficit);
            // Issue new debt
            simulation.getGovernment().getBalanceSheet().increaseDebt(-deficit);

            // TODO: Debt must be added as an asset to other sector
        }
    }
}
