package com.openworldsimulator.economics;

import com.openworldsimulator.simulation.ModelParameters;

public class EconomyParams extends ModelParameters {
    public double _ENABLE_ECONOMY_SIMULATION = 1;

    //
    // Personal consumption params
    //

    public double PROPENSITY_TO_CONSUMPTION_MEAN_RATE = 0.6;
    public double PROPENSITY_TO_CONSUMPTION_STDEV_RATE = 0.1;

    public double MONTHLY_NON_DISCRETIONARY_EXPENSES_MIN = 150;
    public double MONTHLY_NON_DISCRETIONARY_EXPENSES_MEAN = 400;
    public double MONTHLY_NON_DISCRETIONARY_EXPENSES_STDEV = 50;

    public double YIELD_SAVINGS_RATE = 0.00;


    //
    // TAX Params
    //

    public double TAX_ON_DISCRETIONARY_CONSUMPTION_RATE = 0.20;
    public double TAX_ON_NON_DISCRETIONARY_CONSUMPTION_RATE = 0.05;
    public double TAX_ON_INCOME_EMPLOYEE_RATE = 0.20;
    public double TAX_ON_INCOME_EMPLOYER_RATE = 0.20;
    public double TAX_ON_SAVINGS_PCT = 0.20;


    //
    // Job market params
    //

    public double MINIMAL_MONTHLY_WAGE = 800;

    public double PUBLIC_SECTOR_YEARLY_WAGE_STDEV = 500 * 12;
    public double PUBLIC_SECTOR_YEARLY_WAGE_MEAN = 2000 * 12;

    public double PRIVATE_SECTOR_YEARLY_WAGE_STDEV = 500 * 12;
    public double PRIVATE_SECTOR_YEARLY_WAGE_MEAN = 1500 * 12;

    public double PUBLIC_SECTOR_WORK_PCT = 0.15;

    public double FIRST_JOB_AGE_MEAN = 25;
    public double FIRST_JOB_AGE_STDEV = 5;

    public double RETIRE_AGE_MEAN = 64;
    public double RETIRE_AGE_STDEV = 4;


    public double PENSION_REPLACEMENT_RATE = 0.7;
    public double DEBT_INTEREST_RATE = 0.03;

    // TODO: Unemployment, activity rate
    //

}
