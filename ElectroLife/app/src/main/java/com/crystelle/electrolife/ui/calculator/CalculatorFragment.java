package com.crystelle.electrolife.ui.calculator;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crystelle.electrolife.MainActivity;
import com.crystelle.electrolife.R;
import com.crystelle.electrolife.model.TrackedAppliance;
import com.crystelle.electrolife.utils.CalculationsUtil;
import com.crystelle.electrolife.viewmodel.SharedViewModel;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * REVISED: CalculatorFragment handles the main dashboard logic.
 * NEW FEATURE: Integrated MPAndroidChart for dynamic Pie Chart visualization.
 */
public class CalculatorFragment extends Fragment {

    private SharedViewModel sharedViewModel;
    private ApplianceAdapter adapter;
    private TextView tvSummaryDaily, tvSummaryMonthly, tvAppliancesTitle, tvTotalKwhStats;
    private View layoutEmptyState, layoutTotalSummary, layoutTopConsumers, layoutGoalProgress;
    private LinearLayout llTopConsumersList;

    private ProgressBar pbBudget, pbKwh;
    private TextView tvBudgetProgress, tvKwhProgress;
    private ChipGroup cgCategories;

    // NEW: Pie Chart reference
    private PieChart pieChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calculator, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // UI Binding
        tvTotalKwhStats = view.findViewById(R.id.tv_total_kwh_stats);
        tvSummaryDaily = view.findViewById(R.id.tv_summary_daily);
        tvSummaryMonthly = view.findViewById(R.id.tv_summary_monthly);
        tvAppliancesTitle = view.findViewById(R.id.tv_appliances_title);

        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        layoutTotalSummary = view.findViewById(R.id.layout_total_summary);
        layoutTopConsumers = view.findViewById(R.id.layout_top_consumers);
        llTopConsumersList = view.findViewById(R.id.ll_top_consumers_list);

        cgCategories = view.findViewById(R.id.cg_categories);
        if (cgCategories != null) {
            cgCategories.setOnCheckedStateChangeListener((group, checkedIds) ->
                    updateUI(sharedViewModel.getTrackedAppliances().getValue(), sharedViewModel.getElectricityRate().getValue()));
        }

        layoutGoalProgress = view.findViewById(R.id.layout_goal_progress);
        pbBudget = view.findViewById(R.id.pb_budget);
        pbKwh = view.findViewById(R.id.pb_kwh);
        tvBudgetProgress = view.findViewById(R.id.tv_budget_progress);
        tvKwhProgress = view.findViewById(R.id.tv_kwh_progress);

        // NEW: Pie Chart Initialization
        pieChart = view.findViewById(R.id.pie_chart_appliances);
        setupPieChart();

        RecyclerView rvAppliances = view.findViewById(R.id.rv_appliances);
        ExtendedFloatingActionButton fabAdd = view.findViewById(R.id.fab_add_appliance);

        MaterialButton btnArchive = view.findViewById(R.id.btn_archive_month);
        if (btnArchive != null) {
            btnArchive.setOnClickListener(v -> {
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat monthFormatter = new SimpleDateFormat("MMMM", Locale.getDefault());
                String monthName = monthFormatter.format(calendar.getTime());
                int year = calendar.get(Calendar.YEAR);

                sharedViewModel.archiveMonthlyHistory(monthName, year);

                Toast.makeText(getContext(), getString(R.string.archive_success_message, monthName), Toast.LENGTH_SHORT).show();
            });
        }

        adapter = new ApplianceAdapter(new ApplianceAdapter.OnApplianceInteractionListener() {
            @Override public void onRemove(String id) { sharedViewModel.removeAppliance(id); }
            @Override public void onHoursChanged(String id, double hours) { sharedViewModel.updateApplianceHours(id, hours); }
            @Override public void onLimitOverride(String id, double kwhLimit) { sharedViewModel.updateApplianceLimit(id, kwhLimit); }
        });

        rvAppliances.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAppliances.setAdapter(adapter);

        sharedViewModel.getTrackedAppliances().observe(getViewLifecycleOwner(), appliances ->
                updateUI(appliances, sharedViewModel.getElectricityRate().getValue()));

        sharedViewModel.getElectricityRate().observe(getViewLifecycleOwner(), rate ->
                updateUI(sharedViewModel.getTrackedAppliances().getValue(), rate));

        fabAdd.setOnClickListener(v -> ((MainActivity) requireActivity()).showAddApplianceDialog());
    }

    /**
     * Initializes the visual properties of the Pie Chart
     */
    private void setupPieChart() {
        if (pieChart == null) return;

        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setTransparentCircleRadius(0f);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelTextSize(11f);
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setCenterText("Monthly\nCost");
        pieChart.setCenterTextSize(16f);
        pieChart.setCenterTextColor(Color.DKGRAY);
        pieChart.getDescription().setEnabled(false);

        // Hide standard legend to make the chart cleaner
        pieChart.getLegend().setEnabled(false);
    }

    /**
     * Dynamically loads data into the Pie Chart and triggers animations
     */
    private void loadPieChartData(List<TrackedAppliance> appliances, double rate) {
        if (pieChart == null) return;

        List<PieEntry> entries = new ArrayList<>();

        for (TrackedAppliance a : appliances) {
            double monthlyCost = CalculationsUtil.calculateCost(a.getAppliance().getDefaultWattage(), a.getHoursPerDay(), rate).getPerMonth();
            // Only show appliances that are actually costing money
            if (monthlyCost > 0) {
                entries.add(new PieEntry((float) monthlyCost, a.getAppliance().getName()));
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "Appliance Cost Breakdown");

        // Use a nice premium color palette
        ArrayList<Integer> colors = new ArrayList<>();
        for (int c : ColorTemplate.MATERIAL_COLORS) colors.add(c);
        for (int c : ColorTemplate.PASTEL_COLORS) colors.add(c);
        dataSet.setColors(colors);

        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);
        // Add % sign to values
        dataSet.setValueFormatter(new PercentFormatter(pieChart));

        PieData data = new PieData(dataSet);
        pieChart.setData(data);

        // Smoothly animate the chart spinning into existence
        pieChart.animateY(1200, Easing.EaseInOutQuad);
        pieChart.invalidate();
    }

    private void updateUI(List<TrackedAppliance> appliances, Double rate) {
        if (appliances == null || rate == null) return;

        List<TrackedAppliance> filteredAppliances;
        if (cgCategories != null && !cgCategories.getCheckedChipIds().isEmpty()) {
            int checkedId = cgCategories.getCheckedChipIds().get(0);
            Chip selectedChip = cgCategories.findViewById(checkedId);
            String selectedCategoryTag = selectedChip.getText().toString();

            filteredAppliances = appliances.stream()
                    .filter(a -> a.getAppliance().getCategory() != null &&
                            a.getAppliance().getCategory().equalsIgnoreCase(selectedCategoryTag))
                    .collect(Collectors.toList());
        } else {
            filteredAppliances = new ArrayList<>(appliances);
        }

        adapter.setRate(rate);
        adapter.submitList(filteredAppliances);

        if (appliances.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            layoutTotalSummary.setVisibility(View.GONE);
            layoutTopConsumers.setVisibility(View.GONE);
            if (layoutGoalProgress != null) layoutGoalProgress.setVisibility(View.GONE);
            tvAppliancesTitle.setText(R.string.appliances_count_zero);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            layoutTotalSummary.setVisibility(View.VISIBLE);
            tvAppliancesTitle.setText(getString(R.string.appliances_count_format, filteredAppliances.size()));

            double totalDailyKwh = CalculationsUtil.calculateTotalDailyKwh(appliances);

            // UPDATE THIS LINE: Use the new method signature and pass the list of appliances
            double hourlyKwh = CalculationsUtil.calculateTotalHourlyKwh(appliances);

            double monthlyKwh = CalculationsUtil.calculateTotalMonthlyKwh(appliances);
            double totalDailyCost = CalculationsUtil.calculateTotalDailyCost(appliances, rate);
            double totalMonthlyCost = CalculationsUtil.calculateTotalMonthlyCost(appliances, rate);

            if (tvTotalKwhStats != null) {
                tvTotalKwhStats.setText(getString(R.string.energy_stats_format, hourlyKwh, totalDailyKwh, monthlyKwh));
            }

            tvSummaryDaily.setText(CalculationsUtil.formatCurrency(totalDailyCost));
            tvSummaryMonthly.setText(CalculationsUtil.formatCurrency(totalMonthlyCost));

            // Populate and trigger the Pie Chart!
            List<TrackedAppliance> activeAppliances = appliances.stream()
                    .filter(a -> a.getHoursPerDay() > 0)
                    .collect(Collectors.toList());

            if (activeAppliances.isEmpty() && pieChart != null) {
                pieChart.setVisibility(View.GONE);
            } else if (pieChart != null) {
                pieChart.setVisibility(View.VISIBLE);
                loadPieChartData(activeAppliances, rate);
            }

            Double budgetGoal = sharedViewModel.getMonthlyBudgetGoal().getValue();
            Double kwhGoal = sharedViewModel.getMonthlyKwhGoal().getValue();

            if (layoutGoalProgress != null && budgetGoal != null && kwhGoal != null) {
                layoutGoalProgress.setVisibility(View.VISIBLE);

                int budgetPercent = CalculationsUtil.calculateBudgetProgressPercentage(totalMonthlyCost, budgetGoal);
                int kwhPercent = CalculationsUtil.calculateKwhProgressPercentage(monthlyKwh, kwhGoal);

                if (pbBudget != null) pbBudget.setProgress(budgetPercent);
                if (pbKwh != null) pbKwh.setProgress(kwhPercent);

                if (tvBudgetProgress != null) {
                    tvBudgetProgress.setText(getString(R.string.budget_progress_format,
                            CalculationsUtil.formatCurrency(totalMonthlyCost),
                            CalculationsUtil.formatCurrency(budgetGoal)));
                }

                if (tvKwhProgress != null) {
                    tvKwhProgress.setText(getString(R.string.kwh_progress_format, monthlyKwh, kwhGoal));
                }
            }

            // Top Consumers List
            List<TrackedAppliance> topConsumers = appliances.stream()
                    .filter(a -> a.getHoursPerDay() > 0)
                    .sorted((a, b) -> Double.compare(
                            b.getAppliance().getDefaultWattage() * b.getHoursPerDay(),
                            a.getAppliance().getDefaultWattage() * a.getHoursPerDay()
                    ))
                    .collect(Collectors.toList());

            if (topConsumers.isEmpty()) {
                layoutTopConsumers.setVisibility(View.GONE);
            } else {
                layoutTopConsumers.setVisibility(View.VISIBLE);
                llTopConsumersList.removeAllViews();

                int count = Math.min(3, topConsumers.size());
                for (int i = 0; i < count; i++) {
                    TextView tv = new TextView(getContext());
                    TrackedAppliance item = topConsumers.get(i);
                    double monthly = CalculationsUtil.calculateCost(item.getAppliance().getDefaultWattage(), item.getHoursPerDay(), rate).getPerMonth();

                    tv.setText(getString(R.string.top_consumer_item_format,
                            item.getAppliance().getName(),
                            CalculationsUtil.formatCurrency(monthly)));

                    tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_800));
                    tv.setPadding(0, 4, 0, 4);
                    llTopConsumersList.addView(tv);
                }
            }
        }
    }
}