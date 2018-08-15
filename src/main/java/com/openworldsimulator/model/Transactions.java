package com.openworldsimulator.model;

public class Transactions {
    private Companies companies;
    private PublicSector government;
    private Population population;
    private Banks banks;

    public Transactions(Companies companies, PublicSector government, Population population, Banks banks) {
        this.companies = companies;
        this.government = government;
        this.population = population;
        this.banks = banks;
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

    public void payTaxes(Person p, double amount) {
        p.balanceSheet.decreaseSavings(amount);
        government.balanceSheet.increaseSavings(amount);
        government.monthlyResults.addIncome(amount);
    }

    public void payTaxes(Companies c, double amount) {
        c.balanceSheet.decreaseSavings(amount);
        government.balanceSheet.increaseSavings(amount);
        government.monthlyResults.addIncome(amount);
    }

    public void expend(Person p, double amount) {
        p.balanceSheet.decreaseSavings(amount);
        companies.balanceSheet.increaseSavings(amount);
        companies.monthlyResults.addIncome(amount);
    }

    public void earnIncomeFromCompanies(Person p, double amount) {
        p.balanceSheet.increaseSavings(amount);
        companies.balanceSheet.decreaseSavings(amount);
        companies.monthlyResults.addExpenses(amount);
    }

    public void earnIncomeFromBanks(Person p, double amount) {
        p.balanceSheet.increaseSavings(amount);

        // TODO: Total balance sheet of banks remain unchanged? It should probably be finance by a decrease in reserves
        //banks.balanceSheet.decreaseSavings(amount);
    }

    public void earnIncomeFromGovernment(Person p, double amount) {
        p.balanceSheet.increaseSavings(amount);
        government.balanceSheet.decreaseSavings(amount);
        government.monthlyResults.addExpenses(amount);
    }

    public void pay(double amount, Person issuer, Person receiver) {
        issuer.getBalanceSheet().decreaseSavings(amount);
        receiver.getBalanceSheet().increaseSavings(amount);

        // Banks balance sheet remains unchanged
    }
}
