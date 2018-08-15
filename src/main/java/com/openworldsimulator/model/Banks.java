package com.openworldsimulator.model;

public class Banks {
    BalanceSheet balanceSheet = new BalanceSheet("Bank system");
    MonthlyResults monthlyResults = new MonthlyResults();

    public BalanceSheet getBalanceSheet() {
        return balanceSheet;
    }

    public MonthlyResults getMonthlyResults() {
        return monthlyResults;
    }
}
