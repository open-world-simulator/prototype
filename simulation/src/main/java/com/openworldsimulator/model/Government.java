package com.openworldsimulator.model;

public class Government {

    public static final String TYPE_EXPENSES_DEBT = "EXPENSES_DEBT";
    public static final String TYPE_EXPENSES_PENSIONS = "EXPENSES_PENSIONS";
    public static final String TYPE_EXPENSES_EMPLOYMENT = "EXPENSES_EMPLOYMENT";
    public static final String TYPE_EXPENSES_OTHER = "EXPENSES_OTHER";

    public static final String TYPE_INCOME_TAXES_INCOME = "TAXES_INCOME";
    public static final String TYPE_INCOME_TAXES_CONSUMPTION = "TAXES_CONSUMPTION";
    public static final String TYPE_INCOME_TAXES_BUSINESS = "TAXES_BUSINESS";
    public static final String TYPE_INCOME_TAXES_SAVINGS = "TAXES_SAVINGS";

    private BalanceSheet balanceSheet = new BalanceSheet("Government");

    private MonthlyResults monthlyResults = new MonthlyResults();

    public BalanceSheet getBalanceSheet() {
        return balanceSheet;
    }

    public MonthlyResults getMonthlyResults() {
        return monthlyResults;
    }
}
