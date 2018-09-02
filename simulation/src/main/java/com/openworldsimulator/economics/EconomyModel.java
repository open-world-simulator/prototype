package com.openworldsimulator.economics;

import com.openworldsimulator.model.PopulationSegment;
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
    private MicroEconomyStats modelStats;
    private MacroEconomyStats macroStats;

    public EconomyModel(Simulation simulation, File outputPath, EconomyParams params) {
        super(outputPath, simulation);
        this.params = params;
    }

    @Override
    public String getId() {
        return MODEL_ID;
    }

    @Override
    public ModelStats getStats() {

        if( !isEnabled()) {
            return null;
        }

        return modelStats;
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

        if( !isEnabled()) {
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

        modelStats = new MicroEconomyStats(params, simulation);
        macroStats = new MacroEconomyStats(simulation);
    }


    void initEconomicData(PopulationSegment p) {
        p.initialFirstJobAge = RandomTools.random(params.FIRST_JOB_AGE_MEAN, params.FIRST_JOB_AGE_STDEV);
        p.retirementAge = RandomTools.random(params.RETIRE_AGE_MEAN, params.RETIRE_AGE_STDEV);
        p.economicStatus = PopulationSegment.ECONOMIC_STATUS.NONE;
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
        if( !isEnabled()) {
            return;
        }

        log("\n[ECONOMICS]", month, (month) / 12D);

        List<PopulationSegment> people = simulation.getPopulation().getPopulationSegments();

        for (PopulationSegment p : people) {
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

        log("[END Economics %d]", simulation.getPopulation().size());
    }

    @Override
    public void postSimulation(int month) {
        if( !isEnabled()) {
            return;
        }

        // Build monthly stats
        modelStats.collect(month);

        simulatePublicSectorDebtService(month);

        // Build monthly stats
        macroStats.collect(month);

        // Evolve parameters monthly, if needed
        simulation.evolveParametersMonthly(params);
    }

    void simulateConsumptionDebt(int month, PopulationSegment p) {

    }

    void simulateInvestmentDecisions(int month, PopulationSegment p) {
        // TODO
    }


    void simulateEmploymentStatus(int month, PopulationSegment populationSegment) {
        if (!populationSegment.isAlive()) return;
        if (populationSegment.age >= populationSegment.initialFirstJobAge && populationSegment.monthlyData.monthIncomeWage == 0) {
            // Find a job
            if( RandomTools.random(params.JOB_MARKET_PUBLIC_SECTOR_RATE) ) {
                populationSegment.economicStatus = PopulationSegment.ECONOMIC_STATUS.WORKING_PUBLIC_SECTOR;
                populationSegment.grossMonthlySalary = RandomTools.random(params.PUBLIC_SECTOR_MONTHLY_WAGE_MEAN, params.PUBLIC_SECTOR_MONTHLY_WAGE_STDEV);
            } else {
                populationSegment.economicStatus = PopulationSegment.ECONOMIC_STATUS.WORKING_PRIVATE_SECTOR;
                populationSegment.grossMonthlySalary = RandomTools.random(params.PRIVATE_SECTOR_MONTHLY_WAGE_MEAN, params.PRIVATE_SECTOR_MONTHLY_WAGE_STDEV);
            }
            if (populationSegment.grossMonthlySalary < params.MINIMAL_WAGE) {
                populationSegment.grossMonthlySalary = params.MINIMAL_WAGE;
            }
            log("[JOBS] PopulationSegment " + populationSegment.id + " finds job of %.2f at age %.2f", populationSegment.grossMonthlySalary, populationSegment.age);
        }
    }

    void simulateJobIncome(int month, PopulationSegment populationSegment) {
        if (!populationSegment.isAlive()) return;

        // TODO: Unemployment
        if (populationSegment.age >= populationSegment.initialFirstJobAge && populationSegment.age < populationSegment.retirementAge) {
            populationSegment.monthlyData.monthIncomeWage = populationSegment.grossMonthlySalary;

            double employeeTaxes = populationSegment.monthlyData.monthIncomeWage  * params.TAX_ON_INCOME_EMPLOYEE_RATE;
            double employerTaxes = populationSegment.monthlyData.monthIncomeWage  * params.TAX_ON_INCOME_EMPLOYER_RATE;

            if( populationSegment.economicStatus.equals(PopulationSegment.ECONOMIC_STATUS.WORKING_PRIVATE_SECTOR)) {
                simulation.getTransactions().earnIncomeFromCompanies(populationSegment, populationSegment.monthlyData.monthIncomeWage);
                simulation.getTransactions().payTaxes(populationSegment, employeeTaxes);
                simulation.getTransactions().payTaxes(simulation.getCompanies(), employerTaxes);
            } else {
                // Public sector worker
                simulation.getTransactions().earnIncomeFromGovernment(populationSegment, populationSegment.monthlyData.monthIncomeWage);
                simulation.getTransactions().payTaxes(populationSegment, employeeTaxes);
            }

            populationSegment.monthlyData.monthTaxesIncome += employeeTaxes;
        }
    }

    void simulateRetirementIncome(int month, PopulationSegment populationSegment) {
        if (!populationSegment.isAlive()) return;

        if (populationSegment.age >= populationSegment.retirementAge && populationSegment.monthlyData.monthIncomePension == 0) {
            populationSegment.grossPension = populationSegment.monthlyData.monthIncomeWage * params.PENSION_REPLACEMENT_RATE;
            populationSegment.monthlyData.monthIncomeWage = 0;
            populationSegment.economicStatus = PopulationSegment.ECONOMIC_STATUS.RETIRED;
            log("PopulationSegment " + populationSegment.id + " starts receiving avgMonthlyIncomePension of %.2f at age %.2f", populationSegment.monthlyData.monthIncomePension, populationSegment.age);
        }

        populationSegment.monthlyData.monthIncomePension  = populationSegment.grossPension;

        if( populationSegment.monthlyData.monthIncomePension == 0) {
            return;
        }

        double taxes = populationSegment.monthlyData.monthIncomePension * params.TAX_ON_INCOME_EMPLOYEE_RATE;

        simulation.getTransactions().earnIncomeFromGovernment(populationSegment, populationSegment.monthlyData.monthIncomePension);
        simulation.getTransactions().payTaxes(populationSegment, taxes);

        populationSegment.monthlyData.monthTaxesIncome += taxes;
    }

    void simulateEconomicDeath(int month, PopulationSegment populationSegment) {

//        simulateInheritance(month, populationSegment);

        // Remove sources of income
        populationSegment.monthlyData.reset();

        populationSegment.getBalanceSheet().reset();

        populationSegment.economicStatus = PopulationSegment.ECONOMIC_STATUS.NONE;

        log("PopulationSegment " + populationSegment.id + " economic death at age %.2f", populationSegment.age);
    }

    void simulateInheritance(int month, PopulationSegment populationSegment) {
        // Assign all assets to one random living populationSegment
        List<PopulationSegment> alive = simulation.getPopulation().getAlivePeople();
        if (alive.size() > 0) {
            PopulationSegment receiver = alive.get(RandomTools.random(alive.size()));

            simulation.getTransactions().pay(
                    populationSegment.getBalanceSheet().getSavings(),
                    populationSegment,
                    receiver
            );

            log("[INHERITANCE] PopulationSegment %s received %.2f", receiver.toString(),
                    receiver.getBalanceSheet().getSavings()
            );
        } else {
            log("[ERROR] No alive people left to receive inheritance");
        }
    }

    void simulateSavingsYieldIncome(int month, PopulationSegment populationSegment) {

        populationSegment.monthlyData.monthIncomeSavings = populationSegment.getBalanceSheet().getSavings() * params.YIELD_SAVINGS_RATE / 12D;

        double taxes = populationSegment.monthlyData.monthIncomeSavings * params.TAX_ON_SAVINGS_PCT;

        // Cash in interests
        simulation.getTransactions().earnIncomeFromBanks(populationSegment, populationSegment.monthlyData.monthIncomeSavings);
        simulation.getTransactions().payTaxes(populationSegment, taxes);

        populationSegment.monthlyData.monthTaxesFinancial += taxes;
    }


    void simulateNonDiscretionaryExpenses(int month, PopulationSegment populationSegment) {
        if (!populationSegment.isAlive()) return;

        // TODO: Simulate based on income and situation (i.e age)
        populationSegment.monthlyData.monthExpensesNonDiscretionary = RandomTools.random(
                params.MONTHLY_NON_DISCRETIONARY_EXPENSES_MEAN,
                params.MONTHLY_NON_DISCRETIONARY_EXPENSES_STDEV,
                params.MONTHLY_NON_DISCRETIONARY_EXPENSES_MIN,
                -1
        );

        if (populationSegment.monthlyData.monthExpensesNonDiscretionary > populationSegment.getBalanceSheet().getSavings()) {
            log("[EXPENSES1] PopulationSegment %d - Savings: %.2f Expenses: %.2f", populationSegment.id, populationSegment.getBalanceSheet().getSavings(), populationSegment.monthlyData.monthExpensesNonDiscretionary);

            // TODO: Simulate debt or asset liquidation
            // populationSegment.monthExpensesNonDiscretionary = populationSegment.getSavings();
        }

        double taxes = populationSegment.getMonthlyData().monthExpensesNonDiscretionary * params.TAX_ON_NON_DISCRETIONARY_CONSUMPTION_RATE;

        simulation.getTransactions().expend(populationSegment, populationSegment.monthlyData.monthExpensesNonDiscretionary);
        simulation.getTransactions().payTaxes(populationSegment, taxes);

        populationSegment.getMonthlyData().monthExpensesNonDiscretionary += taxes;

        // Assume no debt yet
    }

    void simulateDiscretionaryExpenses(int month, PopulationSegment populationSegment) {
        if (!populationSegment.isAlive()) return;

        double remainingIncome = populationSegment.monthlyData.getTotalMonthIncome() - populationSegment.monthlyData.getTotalExpenses() - populationSegment.monthlyData.getTotalTaxes();
        if (remainingIncome < 0) {
            // Income < non discretionary expenses => don't expend additional
            remainingIncome = 0;
        }

        populationSegment.monthlyData.monthExpensesDiscretionary = RandomTools.random(
                params.PROPENSITY_TO_CONSUMPTION_MEAN_RATE,
                params.PROPENSITY_TO_CONSUMPTION_STDEV_RATE
        ) * (1 - params.TAX_ON_DISCRETIONARY_CONSUMPTION_RATE) * remainingIncome;

        if (populationSegment.monthlyData.monthExpensesDiscretionary > populationSegment.getBalanceSheet().getSavings()) {
            log("[EXPENSES2] PopulationSegment %d - Savings: %.2f Expenses: %.2f", populationSegment.id, populationSegment.getBalanceSheet().getSavings(), populationSegment.monthlyData.monthExpensesDiscretionary);

            // TODO: Simulate debt or asset liquidation
            //populationSegment.monthExpensesDiscretionary = populationSegment.getSavings();
        }

        double taxes = populationSegment.getMonthlyData().monthExpensesDiscretionary * params.TAX_ON_DISCRETIONARY_CONSUMPTION_RATE;

        simulation.getTransactions().expend(populationSegment, populationSegment.monthlyData.monthExpensesDiscretionary);
        simulation.getTransactions().payTaxes(populationSegment, taxes);

        populationSegment.getMonthlyData().monthExpensesDiscretionary += taxes;
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
