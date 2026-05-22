package com.crystelle.electrolife.utils;

import com.crystelle.electrolife.model.CostBreakdown;
import com.crystelle.electrolife.model.TrackedAppliance;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * REVISED: CalculationsUtil centralizes all math logic for ElectroLife.
 * FIX: Resolved "never used" warnings by implementing method delegation.
 * Task 1: Replaced manual division in Fragment with calculateHourlyKwh.
 * Task 2: Synchronized progress bar logic with specialized percentage helpers.
 * Task 3: Implemented explicit step-by-step formula goals for Daily/Monthly kWh and Cost.
 * Task 4: Added strict NullPointer and API backwards-compatibility crash protections.
 */
public class CalculationsUtil {

    private static final int DAYS_IN_MONTH = 30;

    // GOAL 1: Calculate Daily Energy Consumption (kWh)
    public static double calculateDailyKwh(double wattage, double hoursPerDay) {
        return (wattage / 1000.0) * hoursPerDay;
    }

    // GOAL 3: Calculate Daily Cost (PHP)
    public static double calculateDailyCost(double dailyKwh, double rate) {
        return dailyKwh * rate;
    }

    // GOAL 4: Calculate Monthly Cost (PHP)
    public static double calculateMonthlyCost(double dailyCost) {
        return dailyCost * DAYS_IN_MONTH;
    }

    public static double calculateTotalMonthlyCost(List<TrackedAppliance> appliances, double rate) {
        double totalMonthlyCost = 0;
        if (appliances != null && !appliances.isEmpty()) {
            for (TrackedAppliance ta : appliances) {
                // Crash Protection: Skip corrupt or incomplete database entries
                if (ta == null || ta.getAppliance() == null) continue;

                double wattage = ta.getAppliance().getDefaultWattage();
                double hours = ta.getHoursPerDay();

                double dailyKwh = calculateDailyKwh(wattage, hours);
                double dailyCost = calculateDailyCost(dailyKwh, rate);
                double monthlyCost = calculateMonthlyCost(dailyCost);

                totalMonthlyCost += monthlyCost;
            }
        }
        return totalMonthlyCost;
    }

    public static double calculateTotalMonthlyKwh(List<TrackedAppliance> appliances) {
        double totalMonthlyKwh = 0;
        if (appliances != null && !appliances.isEmpty()) {
            for (TrackedAppliance ta : appliances) {
                if (ta == null || ta.getAppliance() == null) continue;

                double wattage = ta.getAppliance().getDefaultWattage();
                double hours = ta.getHoursPerDay();

                double dailyKwh = calculateDailyKwh(wattage, hours);
                double monthlyKwh = dailyKwh * DAYS_IN_MONTH; // GOAL 2: Daily kWh × 30

                totalMonthlyKwh += monthlyKwh;
            }
        }
        return totalMonthlyKwh;
    }

    public static double calculateTotalDailyCost(List<TrackedAppliance> appliances, double rate) {
        double totalDailyCost = 0;
        if (appliances != null && !appliances.isEmpty()) {
            for (TrackedAppliance ta : appliances) {
                if (ta == null || ta.getAppliance() == null) continue;

                double wattage = ta.getAppliance().getDefaultWattage();
                double hours = ta.getHoursPerDay();

                double dailyKwh = calculateDailyKwh(wattage, hours);
                double dailyCost = calculateDailyCost(dailyKwh, rate);

                totalDailyCost += dailyCost;
            }
        }
        return totalDailyCost;
    }

    public static double calculateTotalDailyKwh(List<TrackedAppliance> appliances) {
        double totalDailyKwh = 0;
        if (appliances != null && !appliances.isEmpty()) {
            for (TrackedAppliance ta : appliances) {
                if (ta == null || ta.getAppliance() == null) continue;

                double wattage = ta.getAppliance().getDefaultWattage();
                double hours = ta.getHoursPerDay();

                double dailyKwh = calculateDailyKwh(wattage, hours);

                totalDailyKwh += dailyKwh;
            }
        }
        return totalDailyKwh;
    }

    // REMOVE THIS METHOD:
    // public static double calculateHourlyKwh(double totalDailyKwh) {
    //     return totalDailyKwh / 24.0;
    // }

    // REPLACE WITH THIS:
    public static double calculateTotalHourlyKwh(List<TrackedAppliance> appliances) {
        double totalHourlyKwh = 0;
        if (appliances != null && !appliances.isEmpty()) {
            for (TrackedAppliance ta : appliances) {
                if (ta == null || ta.getAppliance() == null) continue;

                // Hourly kWh is simply the wattage converted to kW
                totalHourlyKwh += (ta.getAppliance().getDefaultWattage() / 1000.0);
            }
        }
        return totalHourlyKwh;
    }

    public static int calculateProgressPercentage(double currentAmount, double goalAmount) {
        if (goalAmount <= 0) return 0;
        double percentage = (currentAmount / goalAmount) * 100;

        // Crash Protection: Prevent NaN or Infinite Math calculations from reaching the UI
        if (Double.isNaN(percentage) || Double.isInfinite(percentage)) return 0;

        return (int) Math.min(Math.round(percentage), 100);
    }

    public static int calculateBudgetProgressPercentage(double currentCost, double budgetGoal) {
        return calculateProgressPercentage(currentCost, budgetGoal);
    }

    public static int calculateKwhProgressPercentage(double currentKwh, double kwhGoal) {
        return calculateProgressPercentage(currentKwh, kwhGoal);
    }

    public static CostBreakdown calculateCost(double wattage, double hoursPerDay, double rate) {
        double kwhPerHour = wattage / 1000.0;
        double costPerHour = kwhPerHour * rate;

        double dailyKwh = calculateDailyKwh(wattage, hoursPerDay);
        double costPerDay = calculateDailyCost(dailyKwh, rate);
        double costPerMonth = calculateMonthlyCost(costPerDay);

        return new CostBreakdown(costPerHour, costPerDay, costPerMonth);
    }

    public static double calculateMonthlyKwh(double wattage, double hoursPerDay) {
        double dailyKwh = calculateDailyKwh(wattage, hoursPerDay);
        return dailyKwh * DAYS_IN_MONTH; // GOAL 2: Daily kWh × 30
    }

    public static String formatCurrency(double amount) {
        // Crash Protection: Changed to older Locale constructor to support API levels below 21
        Locale phLocale = new Locale("en", "PH");
        NumberFormat formatter = NumberFormat.getCurrencyInstance(phLocale);
        return formatter.format(amount);
    }

    public static String formatWattage(double wattage) {
        // Crash Protection: Changed to older Locale constructor to support API levels below 21
        Locale phLocale = new Locale("en", "PH");
        if (wattage >= 1000) {
            return String.format(phLocale, "%.1f kW", wattage / 1000.0);
        }
        return String.format(phLocale, "%.0f W", wattage);
    }
}