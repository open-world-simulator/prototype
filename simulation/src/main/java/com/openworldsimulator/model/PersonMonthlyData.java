package com.openworldsimulator.model;

public class PersonMonthlyData {

    // Income
    public double incomeWage = 0;
    public double incomePension = 0;
    public double incomeSavings = 0;

    // taxes
    public double taxesIncome = 0;
    public double taxesFinancial = 0;
    public double taxesConsumption = 0;

    // Expenses
    public double consumptionNonDiscretionary = 0;
    public double consumptionDiscretionary = 0;

    public double getTotalMonthIncome() {
        return incomeWage + incomePension + incomeSavings;
    }

    public double getTotalMonthNetResult() {
        return getTotalMonthIncome() - getTotalExpenses();
    }

    public double getTotalTaxes() {
        return taxesIncome + taxesFinancial + taxesConsumption;
    }

    public double getTotalConsumption() {
        return consumptionNonDiscretionary + consumptionDiscretionary;
    }

    public double getTotalExpenses() {
        return getTotalConsumption() + getTotalTaxes();
    }

    public void reset() {
        consumptionDiscretionary = 0;
        consumptionNonDiscretionary = 0;
        taxesConsumption = 0;
        taxesFinancial = 0;
        taxesIncome = 0;
        incomePension = 0;
        incomeWage = 0;
        incomeSavings = 0;
    }
}
