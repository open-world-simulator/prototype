package com.openworldsimulator.model;

import java.util.HashMap;
import java.util.Map;

public class MonthlyResults {
    private double monthlyIncome = 0;
    private double monthlyExpenses = 0;
    private Map<String, Double> expensesByType = new HashMap<>();
    private Map<String, Double> incomeByType = new HashMap<>();

    public void reset() {
        monthlyExpenses = 0;
        monthlyIncome = 0;
        expensesByType.clear();
        incomeByType.clear();
    }

    public void addExpenses(double amount) {
        addExpenses(amount, null);
    }

    public void addExpenses(double amount, String typeOfExpense) {
        monthlyExpenses += amount;
        add(expensesByType, amount, typeOfExpense);
    }

    public void addIncome(double amount) {
        addIncome(amount, null);
    }

    public void addIncome(double amount, String typeOfIncome) {
        monthlyIncome += amount;
        add(incomeByType, amount, typeOfIncome);
    }

    private void add(Map<String, Double> map, double amount, String type) {
        if (type != null) {
            Double sum = map.get(type);
            if (sum == null) {
                sum = 0D;
            }
            sum += amount;
            map.put(type, sum);
        }
    }

    public double getMonthlyIncome() {
        return monthlyIncome;
    }

    public double getMonthlyIncome(String type) {
        Double d = incomeByType.get(type);
        return (d == null) ? 0D : d;
    }

    public double getMonthlyExpenses() {
        return monthlyExpenses;
    }

    public double getMonthlyExpenses(String type) {
        Double d = expensesByType.get(type);
        return (d == null) ? 0D : d;
    }

    public double getMonthlyResult() {
        return monthlyIncome - monthlyExpenses;
    }
}
