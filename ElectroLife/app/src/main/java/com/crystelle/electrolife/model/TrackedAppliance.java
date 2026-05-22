package com.crystelle.electrolife.model;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * REVISED: Annotated as a Room Entity for Historical Analytics.
 * FEATURE REMOVAL: Stripped out real-time stopwatch tracking variables.
 * Now relies strictly on manual user input for hoursPerDay.
 */
@Entity(tableName = "tracked_appliances")
public class TrackedAppliance {

    @PrimaryKey
    @NonNull
    private String id;

    @Embedded(prefix = "app_")
    private Appliance appliance;

    private double hoursPerDay;
    private boolean isEnergyVampire;
    private double customKwhLimit;

    // REQUIRED BY ROOM: Empty default constructor
    @SuppressWarnings("unused")
    public TrackedAppliance() {
        this.id = "";
    }

    /**
     * Standard constructor used by the app. Marked @Ignore so Room skips it.
     */
    @Ignore
    public TrackedAppliance(@NonNull String id, Appliance appliance, double hoursPerDay) {
        this.id = id;
        this.appliance = appliance;
        this.hoursPerDay = hoursPerDay;
        this.isEnergyVampire = false;
        this.customKwhLimit = 0.0;
    }

    @Ignore
    public TrackedAppliance(@NonNull String id, Appliance appliance, double hoursPerDay, double customKwhLimit) {
        this.id = id;
        this.appliance = appliance;
        this.hoursPerDay = hoursPerDay;
        this.isEnergyVampire = false;
        this.customKwhLimit = customKwhLimit;
    }

    // --- Core Entity Accessors ---

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public Appliance getAppliance() { return appliance; }
    public void setAppliance(Appliance appliance) { this.appliance = appliance; }

    public double getHoursPerDay() { return hoursPerDay; }
    public void setHoursPerDay(double hoursPerDay) { this.hoursPerDay = hoursPerDay; }

    public boolean isEnergyVampire() { return isEnergyVampire; }
    public void setEnergyVampire(boolean energyVampire) { this.isEnergyVampire = energyVampire; }

    public double getCustomKwhLimit() { return customKwhLimit; }
    public void setCustomKwhLimit(double customKwhLimit) { this.customKwhLimit = customKwhLimit; }

    public boolean hasCustomLimit() { return this.customKwhLimit > 0.0; }

    @NonNull
    @Override
    public String toString() {
        return "TrackedAppliance {" +
                "id='" + id + '\'' +
                ", appliance=" + (appliance != null ? appliance.getName() : "null") +
                ", hours=" + hoursPerDay +
                '}';
    }
}