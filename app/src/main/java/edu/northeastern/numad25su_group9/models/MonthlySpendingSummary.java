package edu.northeastern.numad25su_group9.models;

/**
 * Represents a monthly spending summary.
 */
public class MonthlySpendingSummary {
    private int month;
    private int year;
    private double amount;

    /**
     * Create a monthly spending summary.
     */
    public MonthlySpendingSummary(int month, int year, double amount) {
        this.month = month;
        this.year = year;
        this.amount = amount;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
