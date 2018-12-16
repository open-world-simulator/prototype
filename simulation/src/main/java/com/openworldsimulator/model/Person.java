package com.openworldsimulator.model;

public class Person {
    public BalanceSheet balanceSheet = new BalanceSheet("Person" );
    public PersonMonthlyData monthlyData = new PersonMonthlyData();

    public enum GENDER {MALE, FEMALE}

    public enum LIFE_STATUS {ALIVE, DEAD, GONE}

    // ID for debugging purposes
    public int id;
    public double age;
    public GENDER gender;
    public LIFE_STATUS status = LIFE_STATUS.ALIVE;

    public int deathMonth = -1;
    public int bornMonth = -1;
    public int immigrationMonth = -1;
    public double immigrationAge = 0;
    public int emigrationMonth = -1;

    // Life expectancy at birth
    public double initialLifeExpectancy = 0;

    // Maternity variables
    public int numChildren = 0;
    public double lastChildAge = 0;
    public int initialExpectedChildren = 0;
    public double initialFirstChildAge = 0;
    public double mothersAgeAtBirth = 0;

    /*
     * Micro-economics
     */

    public enum ECONOMIC_STATUS {NONE, UNEMPLOYED, WORKING_PRIVATE_SECTOR, WORKING_PUBLIC_SECTOR, RETIRED}

    // Job variables
    public double initialFirstJobAge = 0;
    public double retirementAge = 0;
    public double grossMonthlySalary = 0;
    public double grossPension = 0;


    public ECONOMIC_STATUS economicStatus = ECONOMIC_STATUS.NONE;

    public Person() {
    }

    public BalanceSheet getBalanceSheet() {
        return balanceSheet;
    }

    public PersonMonthlyData getMonthlyData() {
        return monthlyData;
    }

    public boolean isInPopulation() {
        return status != LIFE_STATUS.DEAD && status != LIFE_STATUS.GONE;
    }

    public boolean justDead(int month) {
        return deathMonth == month;
    }

    public boolean justBorn(int month) {
        return bornMonth == month;
    }

    public boolean isMale() {
        return gender.equals(GENDER.MALE);
    }

    public boolean isFemale() {
        return gender.equals(GENDER.FEMALE);
    }

    @Override
    public String toString() {
        return "id: " + id + "  age=" + age + " gender=" + gender;
    }
}
