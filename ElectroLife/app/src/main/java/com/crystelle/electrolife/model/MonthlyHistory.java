package com.crystelle.electrolife.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * REVISED: Room Entity for Historical Analytics.
 * Stores a snapshot of a specific month's energy consumption and costs.
 * Includes a unique constraint on month and year to prevent duplicate entries.
 */
@Entity(
        tableName = "monthly_history",
        indices = {@Index(value = {"month", "year"}, unique = true)}
)
public class MonthlyHistory {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String month; // e.g., "January" or "01"

    private int year; // e.g., 2026

    private double totalKwh; // Total consumption for the month

    private double totalCost; // Total expenditure based on the rate at that time

    private double budgetGoal; // The budget goal that was set during that month

    private double kwhGoal; // The energy goal that was set during that month

    // NEW FIELD: Stores the list of appliances in JSON format for historical detail ranking.
    // This acts as our "time capsule" for the exact moment the month was archived.
    @ColumnInfo(name = "appliance_snapshot_json")
    private String applianceSnapshotJson;

    /**
     * Standard Constructor for Room.
     */
    public MonthlyHistory(@NonNull String month, int year, double totalKwh, double totalCost) {
        this.month = month;
        this.year = year;
        this.totalKwh = totalKwh;
        this.totalCost = totalCost;
    }

    // --- GETTERS AND SETTERS ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    @NonNull
    public String getMonth() { return month; }
    public void setMonth(@NonNull String month) { this.month = month; }

    // CRITICAL FIX: Added this method to satisfy HistoryAdapter
    // without breaking the existing SQLite database column mapping.
    @NonNull
    public String getMonthName() { return month; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public double getTotalKwh() { return totalKwh; }
    public void setTotalKwh(double totalKwh) { this.totalKwh = totalKwh; }

    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }

    public double getBudgetGoal() { return budgetGoal; }
    public void setBudgetGoal(double budgetGoal) { this.budgetGoal = budgetGoal; }

    public double getKwhGoal() { return kwhGoal; }
    public void setKwhGoal(double kwhGoal) { this.kwhGoal = kwhGoal; }

    // --- NEW SNAPSHOT GETTERS AND SETTERS ---

    public String getApplianceSnapshotJson() {
        return applianceSnapshotJson;
    }

    public void setApplianceSnapshotJson(String applianceSnapshotJson) {
        this.applianceSnapshotJson = applianceSnapshotJson;
    }

    /**
     * Helper to get a formatted label for charts (e.g., "Jan 2026")
     */
    public String getFormattedDate() {
        return month + " " + year;
    }
}