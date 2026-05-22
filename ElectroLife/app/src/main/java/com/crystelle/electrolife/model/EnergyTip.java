package com.crystelle.electrolife.model;

import androidx.annotation.NonNull;

/**
 * REVISED: Represents a discrete energy-saving recommendation or actionable insight.
 * Task 1: Implements immutability patterns by declaring all member fields as 'final'.
 * This structural change ensures that data remains consistent throughout the object's lifecycle,
 * which is a best practice for data models used in RecyclerView adapters and state management.
 */
public class EnergyTip {

    // REVISION: Private member variables declared as final to satisfy static analysis requirements.
    private final String id;
    private final String title;
    private final String description;
    private final String category;
    private final String impact; // Expected values represent the intensity: "High", "Medium", "Low"

    /**
     * Standard constructor for initializing the EnergyTip entity with immutable state.
     * All parameters are required at the time of instantiation to ensure the object is valid.
     *
     * @param id          A unique string identifier for the tip, used primarily for DiffUtil logic.
     * @param title       A short, catchy headline describing the energy-saving behavior.
     * @param description A comprehensive explanation of the tip and its practical application.
     * @param category    The logical grouping for filtering (e.g., "Kitchen", "Living Room", "General").
     * @param impact      The estimated level of energy savings or environmental impact.
     */
    public EnergyTip(String id, String title, String description, String category, String impact) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.impact = impact;
    }

    /**
     * Retrieves the unique identifier for this specific energy tip.
     * Useful for identifying items in a ListAdapter.
     * * @return The non-null ID string.
     */
    @NonNull
    public String getId() {
        return id;
    }

    /**
     * Retrieves the display title for the recommendation UI.
     * * @return The title string representing the core action.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Retrieves the full instructional description of the energy-saving tip.
     * * @return The detailed description text.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Retrieves the category tag assigned to this tip for logical filtering.
     * * @return The category name (e.g., "General", "Lighting").
     */
    public String getCategory() {
        return category;
    }

    /**
     * Retrieves the impact level, indicating the intensity of potential energy savings.
     * * @return A string representing the savings potential ("High", "Medium", or "Low").
     */
    public String getImpact() {
        return impact;
    }
}