package com.crystelle.electrolife.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crystelle.electrolife.R;
import com.crystelle.electrolife.model.MonthlyHistory;
import com.crystelle.electrolife.viewmodel.SharedViewModel;

// --- MPAndroidChart Imports ---
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * REVISED: HistoryFragment
 * Displays the historical record of archived months using both a RecyclerView list
 * and a visual BarChart mapping total costs over time.
 */
public class HistoryFragment extends Fragment {

    private SharedViewModel sharedViewModel;
    private HistoryAdapter adapter;

    // NEW: Declare the BarChart instance for data visualization
    private BarChart barChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Map original UI components
        RecyclerView rvHistory = view.findViewById(R.id.rv_history);
        TextView tvEmpty = view.findViewById(R.id.tv_empty_history);

        // Map and initialize the BarChart view
        barChart = view.findViewById(R.id.history_bar_chart);
        if (barChart != null) {
            // Clean up the chart visually to match a modern, minimalist aesthetic
            barChart.getDescription().setEnabled(false);
            barChart.setDrawGridBackground(false);
            barChart.getAxisRight().setEnabled(false); // Hides the secondary right-side Y-axis
        }

        adapter = new HistoryAdapter();
        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        rvHistory.setAdapter(adapter);

        // Observe the database and update the list and chart automatically!
        sharedViewModel.getAllHistory().observe(getViewLifecycleOwner(), historyList -> {
            if (historyList == null || historyList.isEmpty()) {
                // Show empty state if there is no historical data
                rvHistory.setVisibility(View.GONE);
                if (barChart != null) barChart.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
            } else {
                // Render data if available
                rvHistory.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.GONE);

                // Submit to RecyclerView
                adapter.submitList(historyList);

                // NEW: Feed data to the visualization chart
                setupBarChart(historyList);
            }
        });
    }

    /**
     * Core logic to transform the MonthlyHistory Room data into MPAndroidChart BarEntries.
     * Maps chronological indices to the X-axis and total costs to the Y-axis.
     * * @param histories The list of archived months fetched from the database.
     */
    private void setupBarChart(List<MonthlyHistory> histories) {
        // Safety check to prevent NullPointerExceptions
        if (barChart == null || histories == null || histories.isEmpty()) {
            if (barChart != null) {
                barChart.setVisibility(View.GONE);
            }
            return;
        }

        barChart.setVisibility(View.VISIBLE);

        ArrayList<BarEntry> entries = new ArrayList<>();

        // Loop through the history to create bars
        // X = chronological index, Y = Total Cost
        for (int i = 0; i < histories.size(); i++) {
            float totalCost = (float) histories.get(i).getTotalCost();
            entries.add(new BarEntry(i, totalCost));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Monthly Cost (₱)");

        // Use ContextCompat to safely fetch the resource color
        if (getContext() != null) {
            dataSet.setColor(ContextCompat.getColor(getContext(), R.color.blue_600));
        }

        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        // Tell the chart to redraw itself with the newly processed data
        barChart.invalidate();
    }
}