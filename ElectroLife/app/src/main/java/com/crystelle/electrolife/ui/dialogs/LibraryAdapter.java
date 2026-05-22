package com.crystelle.electrolife.ui.dialogs;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.crystelle.electrolife.R;
import com.crystelle.electrolife.model.Appliance;

import java.util.ArrayList;
import java.util.List;

/**
 * REVISED: LibraryAdapter for selecting appliances from the pre-defined list.
 * FIX: Resolved visibility scope for ViewHolder and implemented resource-based string formatting.
 * UPDATE: Added custom filtering logic and data preservation for search functionality.
 */
public class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.ViewHolder> {

    private final List<Appliance> appliances;
    // Added appliancesFull to store the original, complete set of data
    private final List<Appliance> appliancesFull;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Appliance appliance);
    }

    public LibraryAdapter(List<Appliance> appliances, OnItemClickListener listener) {
        this.appliances = appliances;
        // Initialize appliancesFull as a copy of the original list
        this.appliancesFull = new ArrayList<>(appliances);
        this.listener = listener;
    }

    /**
     * Custom filter method to manage the appliance list dynamically
     */
    public void filter(String text) {
        appliances.clear();
        if (text == null || text.isEmpty()) {
            // Restore original list if search is empty
            appliances.addAll(appliancesFull);
        } else {
            String filterPattern = text.toLowerCase().trim();
            for (Appliance item : appliancesFull) {
                // Check if appliance name contains the search sequence
                if (item.getName().toLowerCase().contains(filterPattern)) {
                    appliances.add(item);
                }
            }
        }
        // Call notifyDataSetChanged to refresh the UI with filtered matches
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_library_appliance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appliance item = appliances.get(position);
        holder.tvName.setText(item.getName());

        // FIX: Using resource string with placeholder instead of concatenation
        holder.tvWattage.setText(holder.itemView.getContext().getString(R.string.wattage_format, item.getDefaultWattage()));

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return appliances.size();
    }

    // FIX: Changed visibility to public to match the LibraryAdapter's scope
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvWattage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_lib_name);
            tvWattage = itemView.findViewById(R.id.tv_lib_wattage);
        }
    }
}