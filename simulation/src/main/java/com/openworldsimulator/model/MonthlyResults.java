package com.openworldsimulator.model;

public class MonthlyResults {
    public double monthlyIncome = 0;
    public double monthlyExpenses = 0;

    public void reset() {
        monthlyExpenses = 0;
        monthlyIncome = 0;
    }

    public void addIncome(double amount) {
        monthlyIncome += amount;
    }

    public void addExpenses(double amount) {
        monthlyExpenses += amount;
    }

    public double getMonthlyIncome() {
        return monthlyIncome;
    }

    public double getMonthlyExpenses() {
        return monthlyExpenses;
    }

    public double getMonthlyResult() {
        return monthlyIncome - monthlyExpenses;
    }
}
