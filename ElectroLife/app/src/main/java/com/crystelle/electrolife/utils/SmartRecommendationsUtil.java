package com.crystelle.electrolife.utils;

import com.crystelle.electrolife.model.SmartRecommendation;
import com.crystelle.electrolife.model.TrackedAppliance;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SmartRecommendationsUtil {

    private static final double ENERGY_VAMPIRE_THRESHOLD_PERCENT = 0.40;
    private static final int HIGH_USAGE_HOURS = 12;

    public static List<SmartRecommendation> generateSmartRecommendations(
            List<TrackedAppliance> appliances,
            double rate,
            double budgetGoal,
            double kwhGoal) {

        List<SmartRecommendation> recommendations = new ArrayList<>();

        if (appliances == null || appliances.isEmpty()) {
            recommendations.add(createEmptyStateRecommendation());
            return recommendations;
        }

        double totalMonthlyCost = 0;
        double totalMonthlyKwh = 0;
        TrackedAppliance topConsumer = null;
        double maxMonthlyKwhFound = 0;

        for (TrackedAppliance ta : appliances) {
            // ---------------------------------------------------------
            // CRITICAL SAFETY CHECK: Skip corrupted database entries
            // ---------------------------------------------------------
            if (ta.getAppliance() == null) continue;

            ta.setEnergyVampire(false);

            double wattage = ta.getAppliance().getDefaultWattage();
            double hours = ta.getHoursPerDay();

            if (hours <= 0) continue;

            double monthlyKwh = CalculationsUtil.calculateMonthlyKwh(wattage, hours);
            double monthlyCost = monthlyKwh * rate;

            totalMonthlyKwh += monthlyKwh;
            totalMonthlyCost += monthlyCost;

            if (monthlyKwh > maxMonthlyKwhFound) {
                maxMonthlyKwhFound = monthlyKwh;
                topConsumer = ta;
            }

            if (hours >= HIGH_USAGE_HOURS) {
                SmartRecommendation usageAlert = new SmartRecommendation(
                        "High Usage: " + ta.getAppliance().getName(),
                        "This device is running for " + hours + " hours a day.",
                        "warning",
                        "High reduction potential",
                        "Reduce daily usage by 2 hours."
                );
                usageAlert.setActionable("Try using a smart plug or timer for this appliance.");
                recommendations.add(usageAlert);
            }
        }

        if (budgetGoal > 0 && totalMonthlyCost > budgetGoal) {
            double overageAmount = totalMonthlyCost - budgetGoal;
            SmartRecommendation budgetAlert = new SmartRecommendation(
                    "Budget Goal Exceeded",
                    String.format(Locale.getDefault(), "Cost (₱%.2f) is over your monthly budget limit.", totalMonthlyCost),
                    "danger",
                    String.format(Locale.getDefault(), "Target reduction: ₱%.2f", overageAmount),
                    "Check high-consumption appliances."
            );
            budgetAlert.setActionable("Reduce the usage hours of your top consumers.");
            recommendations.add(budgetAlert);
        }

        if (kwhGoal > 0 && totalMonthlyKwh > kwhGoal) {
            SmartRecommendation energyAlert = new SmartRecommendation(
                    "Energy Goal Warning",
                    String.format(Locale.getDefault(), "Total usage (%.1f kWh) is above your target.", totalMonthlyKwh),
                    "warning",
                    "Limit daily consumption",
                    "Review active hours."
            );
            energyAlert.setPotentialSavings("Significant savings possible.");
            recommendations.add(energyAlert);
        }

        if (topConsumer != null && topConsumer.getHoursPerDay() > 0) {
            double dailyGoalKwh = kwhGoal / 30.0;
            double applianceDailyKwh = (topConsumer.getAppliance().getDefaultWattage() / 1000.0) * topConsumer.getHoursPerDay();
            double globalThresholdKwh = dailyGoalKwh * ENERGY_VAMPIRE_THRESHOLD_PERCENT;
            double customLimitKwh = topConsumer.getCustomKwhLimit();

            boolean exceedsGlobal = (kwhGoal > 0 && applianceDailyKwh > globalThresholdKwh);
            boolean exceedsCustom = (customLimitKwh > 0 && applianceDailyKwh > customLimitKwh);

            if (exceedsGlobal || exceedsCustom) {
                topConsumer.setEnergyVampire(true);
                String reason = exceedsCustom ?
                        "exceeds your custom limit of " + String.format(Locale.getDefault(), "%.2f", customLimitKwh) + " kWh." :
                        "takes up more than 40% of your daily energy goal.";

                SmartRecommendation vampireRec = new SmartRecommendation(
                        "Energy Vampire Detected",
                        "Your " + topConsumer.getAppliance().getName() + " " + reason,
                        "danger",
                        "High Savings Impact",
                        "Unplug or set usage limit."
                );
                vampireRec.setActionable("Consider reducing usage for " + topConsumer.getAppliance().getName());
                recommendations.add(vampireRec);
            } else {
                topConsumer.setEnergyVampire(false);
            }
        }

        addGeneralTips(recommendations, appliances);
        return recommendations;
    }

    private static SmartRecommendation createEmptyStateRecommendation() {
        SmartRecommendation emptyRec = new SmartRecommendation(
                "No Appliances Tracked",
                "Add appliances in the Calculator tab to see personalized energy-saving recommendations.",
                "info",
                "No data available",
                "Start by adding a device."
        );
        emptyRec.setPotentialSavings("0.00 kWh");
        emptyRec.setActionable("Tap the floating action button to begin.");
        return emptyRec;
    }

    private static void addGeneralTips(List<SmartRecommendation> list, List<TrackedAppliance> appliances) {
        boolean hasActiveAppliance = false;
        boolean hasAC = false;

        for (TrackedAppliance ta : appliances) {
            // Second safety check for the tips loop
            if (ta.getAppliance() == null) continue;

            if (ta.getHoursPerDay() > 0) {
                hasActiveAppliance = true;
                if ("Cooling".equalsIgnoreCase(ta.getAppliance().getCategory())) {
                    hasAC = true;
                }
            }
        }

        if (hasActiveAppliance) {
            if (hasAC) {
                list.add(new SmartRecommendation(
                        "Cooling Efficiency",
                        "Set AC to 24°C-26°C for optimal savings.",
                        "info",
                        "Up to 10% lower bill",
                        "Adjust thermostat settings."
                ));
            }
            list.add(new SmartRecommendation(
                    "Phantom Loads",
                    "Unplug electronics when not in use to avoid standby power draw.",
                    "info",
                    "Minor daily savings",
                    "Check chargers and peripherals."
            ));
        }
    }
}