package com.openworldsimulator.economics.params;

import com.openworldsimulator.simulation.ModelParameters;

public class JobMarketParams extends ModelParameters {

    public double MINIMAL_WAGE = 800;

    public double PUBLIC_SECTOR_YEARLY_WAGE_STDEV = 500;
    public double PUBLIC_SECTOR_YEARLY_WAGE_MEAN = 2000;

    public double PRIVATE_SECTOR_YEARLY_WAGE_STDEV = 500;
    public double PRIVATE_SECTOR_YEARLY_WAGE_MEAN = 1500;

    public double PUBLIC_SECTOR_WORK_PCT = 0.15;

    public double FIRST_JOB_AGE_MEAN = 25;
    public double FIRST_JOB_AGE_STDEV = 5;

    public double RETIRE_AGE_MEAN = 64;
    public double RETIRE_AGE_STDEV = 4;

}
