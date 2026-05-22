package com.crystelle.electrolife.model;

/**
 * REVISED: Represents a calculated cost structure across time intervals.
 * Task 1: Enforces immutability by using the final keyword for calculated values.
 * This class serves as a data holder for energy expenditure results.
 */
public class CostBreakdown {

    // REVISION: Fields are now final as they are set during initialization and never modified.
    private final double perHour;
    private final double perDay;
    private final double perMonth;

    /**
     * Standard constructor for creating a cost snapshot.
     * Values are assigned once and protected by the final modifier.
     */
    public CostBreakdown(double perHour, double perDay, double perMonth) {
        this.perHour = perHour;
        this.perDay = perDay;
        this.perMonth = perMonth;
    }

    /**
     * @return The electricity cost accumulated over one hour.
     */
    public double getPerHour() {
        return perHour;
    }

    /**
     * @return The electricity cost accumulated over one full day (24 hours).
     */
    public double getPerDay() {
        return perDay;
    }

    /**
     * @return The estimated electricity cost for a 30-day billing cycle.
     */
    public double getPerMonth() {
        return perMonth;
    }
}