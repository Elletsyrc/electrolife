package com.crystelle.electrolife.ui.dialogs;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crystelle.electrolife.R;
import com.crystelle.electrolife.model.Appliance;
import com.crystelle.electrolife.model.TrackedAppliance;
import com.crystelle.electrolife.viewmodel.SharedViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * REVISED: AddApplianceBottomSheet handles the creation of new appliance entries.
 * Task 1: Implements the Tag System by capturing room categories from the ChipGroup.
 * Updates the Appliance model with the selected room before persisting to Room DB.
 * Task 2: Uses explicit setters for Appliance creation to resolve IDE "unused method" warnings.
 */
public class AddApplianceBottomSheet extends BottomSheetDialogFragment {

    private SharedViewModel sharedViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflates the layout containing the TabLayout and ViewFlipper
        return inflater.inflate(R.layout.dialog_add_appliance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Scope to activity to share data with the Calculator fragment
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // --- UI BINDING ---
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        ViewFlipper viewFlipper = view.findViewById(R.id.view_flipper);
        RecyclerView rvLibrary = view.findViewById(R.id.rv_library);

        // Library Search Input Reference
        EditText etLibrarySearch = view.findViewById(R.id.et_library_search);

        // Custom Input References
        EditText etCustomName = view.findViewById(R.id.et_custom_name);
        EditText etCustomWattage = view.findViewById(R.id.et_custom_wattage);
        Button btnAddCustom = view.findViewById(R.id.btn_add_custom);

        // Task 1: Room Tag System reference
        ChipGroup cgCustomRoom = view.findViewById(R.id.cg_custom_room);

        // REVISION: Correctly assigned IDs to resolve "always false" warnings
        // These provide the red error text below the input fields
        TextInputLayout tilCustomName = view.findViewById(R.id.til_custom_name);
        TextInputLayout tilCustomWattage = view.findViewById(R.id.til_custom_wattage);

        // Handle Tab Switching (Library vs Custom)
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewFlipper.setDisplayedChild(tab.getPosition());
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // --- 1. SETUP PREDEFINED LIBRARY ---
        rvLibrary.setLayoutManager(new LinearLayoutManager(getContext()));
        List<Appliance> library = new ArrayList<>();
        library.add(new Appliance("1", "Air Conditioner (Window)", 1200, "Cooling", "wind"));
        library.add(new Appliance("2", "Refrigerator", 150, "Kitchen", "refrigerator"));
        library.add(new Appliance("3", "Desktop PC", 300, "Electronics", "monitor"));
        library.add(new Appliance("4", "LED TV (55 inch)", 80, "Entertainment", "monitor"));
        library.add(new Appliance("5", "Electric Fan", 50, "Cooling", "wind"));
        library.add(new Appliance("6", "Incandescent Bulb", 60, "Lighting", "lightbulb"));
        library.add(new Appliance("7", "LED Bulb", 10, "Lighting", "lightbulb"));

        LibraryAdapter adapter = new LibraryAdapter(library, appliance -> {
            // Task 2: Create tracked instance with unique UUID for Room persistence
            String uniqueId = UUID.randomUUID().toString();
            TrackedAppliance newAppliance = new TrackedAppliance(uniqueId, appliance, 4.0);
            sharedViewModel.addAppliance(newAppliance);
            dismiss();
        });
        rvLibrary.setAdapter(adapter);

        // --- Task 3: CONNECT SEARCH UI TO ADAPTER FILTERING ---
        if (etLibrarySearch != null) {
            etLibrarySearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // Not needed
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Not needed
                }

                @Override
                public void afterTextChanged(Editable s) {
                    // Update the adapter results AFTER the text is fully finalized
                    adapter.filter(s.toString());
                }
            });
        } else {
            // If the search bar fails to filter, this warning will explicitly tell you why!
            android.widget.Toast.makeText(getContext(), "Error: Search bar ID not found. Clean your project.", android.widget.Toast.LENGTH_LONG).show();
        }

        // --- 2. SETUP CUSTOM SUBMISSION & VALIDATION ---
        btnAddCustom.setOnClickListener(v -> {
            String name = etCustomName.getText().toString().trim();
            String wattageStr = etCustomWattage.getText().toString().trim();
            boolean isValid = true;

            // Name Validation with TextInputLayout feedback
            if (name.isEmpty()) {
                if (tilCustomName != null) {
                    tilCustomName.setError("Appliance name is required");
                } else {
                    etCustomName.setError("Appliance name is required");
                }
                isValid = false;
            } else if (tilCustomName != null) {
                tilCustomName.setError(null);
            }

            // Wattage Validation with NumberFormat handling
            double wattage = 0;
            try {
                if (wattageStr.isEmpty()) {
                    if (tilCustomWattage != null) tilCustomWattage.setError("Wattage is required");
                    isValid = false;
                } else {
                    wattage = Double.parseDouble(wattageStr);
                    if (wattage <= 0) {
                        if (tilCustomWattage != null) tilCustomWattage.setError("Must be greater than 0");
                        isValid = false;
                    } else if (tilCustomWattage != null) {
                        tilCustomWattage.setError(null);
                    }
                }
            } catch (NumberFormatException e) {
                if (tilCustomWattage != null) tilCustomWattage.setError("Invalid number");
                isValid = false;
            }

            // Task 1: Capture Selected Category from the ChipGroup
            String selectedRoom = "General";
            if (cgCustomRoom != null && cgCustomRoom.getCheckedChipId() != View.NO_ID) {
                Chip selectedChip = view.findViewById(cgCustomRoom.getCheckedChipId());
                if (selectedChip != null) {
                    selectedRoom = selectedChip.getText().toString();
                }
            } else {
                // Force room selection to maintain categorization integrity
                Toast.makeText(getContext(), "Please select a room category", Toast.LENGTH_SHORT).show();
                isValid = false;
            }

            // Final block before proceeding to persistence
            if (!isValid) return;

            // --- REVISION: Task 2 - Resolve Setters by using explicit calls ---
            // Instead of using the long constructor, we initialize and use setters.
            // This satisfies IDE static analysis that methods are in use.
            String applianceId = "custom_" + System.currentTimeMillis();

            // Requires a default empty constructor or a partial one in Appliance.java
            Appliance customApp = new Appliance("", "", 0.0, "", "");
            customApp.setId(applianceId);
            customApp.setName(name);
            customApp.setDefaultWattage(wattage);
            customApp.setCategory(selectedRoom);
            customApp.setIconName("zap");

            // Create the tracked instance for the calculation logic
            String trackedId = UUID.randomUUID().toString();
            TrackedAppliance newAppliance = new TrackedAppliance(trackedId, customApp, 4.0);

            // Notify ViewModel to save to database and update UI
            sharedViewModel.addAppliance(newAppliance);
            dismiss();
        });
    }
}