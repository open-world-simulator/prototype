package com.openworldsimulator.model;

public class Companies {
    MonthlyResults monthlyResults = new MonthlyResults();

    BalanceSheet balanceSheet = new BalanceSheet("Companies");

    public BalanceSheet getBalanceSheet() {
        return balanceSheet;
    }

    public MonthlyResults getMonthlyResults() {
        return monthlyResults;
    }
}
