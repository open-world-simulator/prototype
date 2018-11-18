package com.openworldsimulator.model;

import com.openworldsimulator.simulation.Simulation;

public class Transactions {
    private Companies companies;
    private Government government;
    private Banks banks;

    public Transactions(Simulation simulation) {
        this.companies = simulation.getCompanies();
        this.government = simulation.getGovernment();
        this.banks = simulation.getBanks();
    }

    public void grantLoan(double amount, Person p) {
        banks.balanceSheet.increaseDebt( amount ); // Create a new loan
        banks.balanceSheet.increaseSavings( amount );

        p.balanceSheet.increaseDebt(amount);
        p.balanceSheet.increaseSavings(amount);
    }

    public void payLoan(double amount, Person p) {
        banks.balanceSheet.decreaseDebt( amount ); // Create a new loan
        banks.balanceSheet.decreaseSavings( amount );

        p.balanceSheet.decreaseDebt(amount); // Create a new loan
        p.balanceSheet.decreaseSavings(amount);
    }

    public void payTaxes(Person p, double amount, String type) {
        p.balanceSheet.decreaseSavings(amount);
        government.getBalanceSheet().increaseSavings(amount);
        government.getMonthlyResults().addIncome(amount, type);
    }

    public void payTaxes(Companies c, double amount, String type) {
        c.getBalanceSheet().decreaseSavings(amount);
        government.getBalanceSheet().increaseSavings(amount);
        government.getMonthlyResults().addIncome(amount, type);
    }

    public void expend(Person p, double amount) {
        p.balanceSheet.decreaseSavings(amount);
        companies.getBalanceSheet().increaseSavings(amount);
        companies.getMonthlyResults().addIncome(amount);
    }

    public void earnIncomeFromCompanies(Person p, double amount) {
        p.balanceSheet.increaseSavings(amount);
        companies.getBalanceSheet().decreaseSavings(amount);
        companies.getMonthlyResults().addExpenses(amount);
    }

    public void earnIncomeFromBanks(Person p, double amount) {
        p.balanceSheet.increaseSavings(amount);

        // TODO: Total balance sheet of banks remain unchanged? It should probably be finance by a decrease in reserves
        //banks.balanceSheet.decreaseSavings(amount);
    }

    public void earnIncomeFromGovernment(Person p, double amount, String typeOfIncome) {
        p.balanceSheet.increaseSavings(amount);
        government.getBalanceSheet().decreaseSavings(amount);
        government.getMonthlyResults().addExpenses(amount, typeOfIncome);
    }

    public void expendGovernment(double amount, String type) {
        government.getBalanceSheet().decreaseSavings(amount);
        // TODO: Government expense is other sector income
        government.getMonthlyResults().addExpenses(amount, type);
    }

    public void pay(double amount, Person issuer, Person receiver) {
        issuer.getBalanceSheet().decreaseSavings(amount);
        receiver.getBalanceSheet().increaseSavings(amount);

        // Banks balance sheet remains unchanged
    }
}
