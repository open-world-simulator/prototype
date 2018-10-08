package com.openworldsimulator.model;

public class PublicSector {
    BalanceSheet balanceSheet = new BalanceSheet("Public Sector");

    MonthlyResults monthlyResults = new MonthlyResults();


    public BalanceSheet getBalanceSheet() {
        return balanceSheet;
    }

    public MonthlyResults getMonthlyResults() {
        return monthlyResults;
    }
}
