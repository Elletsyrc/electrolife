package com.crystelle.electrolife.model;

import androidx.room.Ignore;

/**
 * Aligned with 'Appliance' interface from types.ts
 * Represents a standard appliance from the library.
 * CRITICAL FIX: Added empty constructor for Room database mapping.
 */
public class Appliance {
    private String id;
    private String name;
    private double defaultWattage;
    private String category;
    private String iconName;

    // FIX: Tells Android Studio to stop flagging this as unused. Room needs it!
    @SuppressWarnings("unused")
    public Appliance() {
    }

    @Ignore // Tells Room not to use this constructor, but your app code still can
    public Appliance(String id, String name, double defaultWattage, String category, String iconName) {
        this.id = id;
        this.name = name;
        this.defaultWattage = defaultWattage;
        this.category = category;
        this.iconName = iconName;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public double getDefaultWattage() { return defaultWattage; }
    public String getCategory() { return category; }
    public String getIconName() { return iconName; }

    // Setters (Required for persistence and alignment)
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDefaultWattage(double defaultWattage) { this.defaultWattage = defaultWattage; }
    public void setCategory(String category) { this.category = category; }
    public void setIconName(String iconName) { this.iconName = iconName; }
}