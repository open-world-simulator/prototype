package com.openworldsimulator.economics;

import com.openworldsimulator.simulation.ModelParameters;
import com.openworldsimulator.tools.ModelParametersTools;

public class EconomyParams extends ModelParameters {
    // TODO: Unemployment
    public double _ENABLE_ECONOMY_SIMULATION = 0;

    // Work market modeling
    public double PRIVATE_SECTOR_MONTHLY_WAGE_STDEV = 500;
    public double PRIVATE_SECTOR_MONTHLY_WAGE_MEAN = 1500;

    public double PUBLIC_SECTOR_MONTHLY_WAGE_STDEV = 500;
    public double PUBLIC_SECTOR_MONTHLY_WAGE_MEAN = 2000;

    public double JOB_MARKET_PUBLIC_SECTOR_RATE = 0.15;
    public double MINIMAL_WAGE = 800;

    public double YIELD_SAVINGS_RATE = 0.03;

    // TAX PARAMS
    public double TAX_ON_DISCRETIONARY_CONSUMPTION_RATE = 0.01;
    public double TAX_ON_NON_DISCRETIONARY_CONSUMPTION_RATE = 0.10;
    public double TAX_ON_INCOME_EMPLOYEE_RATE = 0.01;
    public double TAX_ON_INCOME_EMPLOYER_RATE = 0.01;
    public double TAX_ON_SAVINGS_PCT = 0.20;

    ///////////////////////////////////////
    /// Behaviour params

    // Consumption params
    public double PROPENSITY_TO_CONSUMPTION_MEAN_RATE = 0.6;
    public double PROPENSITY_TO_CONSUMPTION_STDEV_RATE = 0.1;

    public double MONTHLY_NON_DISCRETIONARY_EXPENSES_MIN = 150;
    public double MONTHLY_NON_DISCRETIONARY_EXPENSES_MEAN = 400;
    public double MONTHLY_NON_DISCRETIONARY_EXPENSES_STDEV = 1;

    public double FIRST_JOB_AGE_MEAN = 25;
    public double FIRST_JOB_AGE_STDEV = 5;

    public double RETIRE_AGE_MEAN = 50;
    public double RETIRE_AGE_STDEV = 2;

    // Pension % compared to last grossMonthlySalary*
    public double PENSION_REPLACEMENT_RATE = 0.7;

    public double AVERAGE_COST_OF_PUBLIC_DEBT = 0.05;

    @Override
    public String toString() {
        return ModelParametersTools.toString(this);
    }
}
