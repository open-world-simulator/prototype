package com.openworldsimulator.model;

public class BalanceSheet {

    private String id;

    private double savings = 0;

    private double debt = 0;

    public double getSavings() {
        return savings;
    }

    public double getDebt() {
        return debt;
    }

    public BalanceSheet(String id) {
        this.id = id;
    }

    public void reset() {
        savings = 0;
        debt = 0;
    }

    public void increaseSavings(double amount) {
        savings += amount;
    }

    public void decreaseSavings(double amountRequested) {
        if (amountRequested == 0.0) return;
        if (amountRequested < 0) {
            System.out.printf("[Balance sheet %s] Withdrawing negative amount %.2f\n", id, amountRequested);
            return;
        }
        if (savings < amountRequested) {
            if (savings > 0) {
                amountRequested = (amountRequested - savings);
                savings = 0;
            }

            debt += amountRequested;

            //System.out.printf("[Balance sheet %s] Decreasing savings into debt - requested: savings: %.2f debt: %.2f\n", id, savings, debt);

            return;
        }

        savings -= amountRequested;
    }

    public void increaseDebt(double amount) {
        debt += amount;
    }

    public void decreaseDebt(double amount) {
        debt -= amount;
    }

    @Override
    public String toString() {
        return "BalanceSheet{" +
                "id='" + id + '\'' +
                ", savings=" + savings +
                ", debt=" + debt +
                '}';
    }
}
