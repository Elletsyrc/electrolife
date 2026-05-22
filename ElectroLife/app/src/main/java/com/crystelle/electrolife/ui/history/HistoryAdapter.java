package com.crystelle.electrolife.ui.history;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.crystelle.electrolife.model.MonthlyHistory;
import com.crystelle.electrolife.model.TrackedAppliance;
import com.crystelle.electrolife.utils.CalculationsUtil;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * REVISED: HistoryAdapter
 * Handles the display of archived monthly data in the RecyclerView.
 * Now includes custom Double-Tap gesture detection to trigger the
 * detailed appliance ranking dialog using JSON snapshots.
 */
public class HistoryAdapter extends ListAdapter<MonthlyHistory, HistoryAdapter.ViewHolder> {

    public HistoryAdapter() {
        super(new DiffUtil.ItemCallback<MonthlyHistory>() {
            @Override
            public boolean areItemsTheSame(@NonNull MonthlyHistory oldItem, @NonNull MonthlyHistory newItem) {
                // Ensure unique items are identified by their database ID
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull MonthlyHistory oldItem, @NonNull MonthlyHistory newItem) {
                // Compare the core calculated values to see if the UI needs to redraw
                return oldItem.getTotalCost() == newItem.getTotalCost() &&
                        oldItem.getTotalKwh() == newItem.getTotalKwh();
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Reusing the simple list item layout for the history cards
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MonthlyHistory history = getItem(position);

        // Bind the standard text data to the card
        holder.bind(history);

        // --- DOUBLE-TAP GESTURE DETECTION LOGIC ---
        // We use a single-element array to bypass Java's restriction on modifying
        // local variables from inside a lambda expression.
        final long[] lastClickTime = {0};

        holder.itemView.setOnClickListener(v -> {
            long clickTime = System.currentTimeMillis();

            // Check if the current click is within 300 milliseconds of the last click
            if (clickTime - lastClickTime[0] < 300) {
                // 300ms window = Double Tap Detected!
                showDetailDialog(holder.itemView.getContext(), history);
            } else {
                // UX Enhancement: Hint to the user that they can double tap
                Toast.makeText(holder.itemView.getContext(), "Double-tap to view appliance ranking", Toast.LENGTH_SHORT).show();
            }

            // Record this click time for the next comparison
            lastClickTime[0] = clickTime;
        });
    }

    /**
     * Unpacks the JSON time capsule and displays the appliances ranked by energy footprint.
     * * @param context The Context required to build the AlertDialog
     * @param history The specific MonthlyHistory record tapped by the user
     */
    private void showDetailDialog(Context context, MonthlyHistory history) {
        // Guard against null data or missing snapshots from older app versions
        if (history.getApplianceSnapshotJson() == null || history.getApplianceSnapshotJson().isEmpty()) {
            Toast.makeText(context, "Detailed breakdown is not available for this month.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Unpack the JSON string back into a List of TrackedAppliance using Gson
        Type listType = new TypeToken<List<TrackedAppliance>>(){}.getType();
        List<TrackedAppliance> snapshotList = new Gson().fromJson(history.getApplianceSnapshotJson(), listType);

        // 2. Sort the list by highest energy footprint (Wattage * Hours Used)
        // Safely checks the nested getAppliance() object to prevent NullPointerExceptions
        Collections.sort(snapshotList, (a, b) -> {
            double footprintA = (a.getAppliance() != null) ? (a.getAppliance().getDefaultWattage() * a.getHoursPerDay()) : 0;
            double footprintB = (b.getAppliance() != null) ? (b.getAppliance().getDefaultWattage() * b.getHoursPerDay()) : 0;
            return Double.compare(footprintB, footprintA);
        });

        // 3. Build a readable string format for the Dialog
        StringBuilder details = new StringBuilder();
        int rank = 1;
        for (TrackedAppliance app : snapshotList) {
            // Skip any corrupted data where the appliance details might be null
            if (app.getAppliance() == null) continue;

            details.append(rank).append(". ").append(app.getAppliance().getName()).append("\n")
                    .append("   Wattage: ").append(app.getAppliance().getDefaultWattage()).append("W\n")
                    .append("   Usage: ").append(app.getHoursPerDay()).append(" hrs/day\n\n");
            rank++;
        }

        // 4. Show the Pop-up Dialog with the ranking
        new AlertDialog.Builder(context)
                .setTitle("Appliance Ranking: " + history.getMonthName() + " " + history.getYear())
                .setMessage(details.toString())
                .setPositiveButton("Close", null)
                .show();
    }

    /**
     * Internal ViewHolder class responsible for binding the views.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView text1;
        private final TextView text2;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }

        public void bind(MonthlyHistory item) {
            // Set the primary text (Month and Year)
            text1.setText(String.format(Locale.getDefault(), "%s %d", item.getMonthName(), item.getYear()));

            // Set the secondary text (kWh and Formatted Cost)
            String cost = CalculationsUtil.formatCurrency(item.getTotalCost());
            text2.setText(String.format(Locale.getDefault(), "Total Usage: %.2f kWh | Bill: %s", item.getTotalKwh(), cost));
        }
    }
}