package com.openworldsimulator.model;

public class PersonMonthlyData {

    // Income
    public double monthIncomeWage = 0;
    public double monthIncomePension = 0;
    public double monthIncomeSavings = 0;

    // Credit
    public double monthlyMaxLoanLimit = 0;
    public double monthlyRequestedLoan = 0;

    // taxes
    public double monthTaxesIncome = 0;
    public double monthTaxesFinancial = 0;
    public double monthTaxesConsumption = 0;

    // Expenses
    public double monthExpensesNonDiscretionary = 0;
    public double monthExpensesDiscretionary = 0;

    public double getTotalMonthIncome() {
        return monthIncomeWage + monthIncomePension + monthIncomeSavings;
    }

    public double getTotalMonthNetResult() {
        return getTotalMonthIncome() - getTotalTaxes() - getTotalExpenses();
    }

    public double getTotalTaxes() {
        return monthTaxesIncome + monthTaxesFinancial + monthTaxesConsumption;
    }

    public double getTotalExpenses() {
        return monthExpensesNonDiscretionary + monthExpensesDiscretionary;
    }

    public void reset() {
        monthExpensesDiscretionary = 0;
        monthExpensesNonDiscretionary = 0;
        monthTaxesConsumption=0;
        monthTaxesFinancial =0;
        monthTaxesIncome = 0;
        monthIncomePension = 0;
        monthIncomeWage = 0;
        monthIncomeSavings = 0;
    }
}
