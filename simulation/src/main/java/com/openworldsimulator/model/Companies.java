package com.openworldsimulator.model;

public class Companies {
    private MonthlyResults monthlyResults = new MonthlyResults();

    private BalanceSheet balanceSheet = new BalanceSheet("Companies");

    public BalanceSheet getBalanceSheet() {
        return balanceSheet;
    }

    public MonthlyResults getMonthlyResults() {
        return monthlyResults;
    }
}
