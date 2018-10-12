package com.openworldsimulator.economics.params;

import com.openworldsimulator.simulation.ModelParameters;
import com.openworldsimulator.tools.ModelParametersTools;

public class GovParams extends ModelParameters {
    public double TAX_ON_DISCRETIONARY_CONSUMPTION_RATE = 0.01;
    public double TAX_ON_NON_DISCRETIONARY_CONSUMPTION_RATE = 0.10;
    public double TAX_ON_INCOME_EMPLOYEE_RATE = 0.01;
    public double TAX_ON_INCOME_EMPLOYER_RATE = 0.01;
    public double TAX_ON_SAVINGS_PCT = 0.20;

    //
    // Expending
    //

    // Pension % compared to last grossMonthlySalary*
    public double PENSION_REPLACEMENT_RATE = 0.7;

    public double AVERAGE_COST_OF_PUBLIC_DEBT = 0.05;
}
