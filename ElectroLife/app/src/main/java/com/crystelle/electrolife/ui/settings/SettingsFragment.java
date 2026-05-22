package com.crystelle.electrolife.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.crystelle.electrolife.R;
import com.crystelle.electrolife.viewmodel.SharedViewModel;

public class SettingsFragment extends Fragment {

    private SharedViewModel sharedViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        EditText etRate = view.findViewById(R.id.et_electricity_rate);
        EditText etBudget = view.findViewById(R.id.et_budget_goal);
        EditText etKwh = view.findViewById(R.id.et_kwh_goal);
        Button btnSave = view.findViewById(R.id.btn_save_settings);

        // Pre-fill initial values from ViewModel
        if (sharedViewModel.getElectricityRate().getValue() != null)
            etRate.setText(String.valueOf(sharedViewModel.getElectricityRate().getValue()));
        if (sharedViewModel.getMonthlyBudgetGoal().getValue() != null)
            etBudget.setText(String.valueOf(sharedViewModel.getMonthlyBudgetGoal().getValue()));
        if (sharedViewModel.getMonthlyKwhGoal().getValue() != null)
            etKwh.setText(String.valueOf(sharedViewModel.getMonthlyKwhGoal().getValue()));

        // REVISION: Set up a single click listener for the Save button
        btnSave.setOnClickListener(v -> {
            try {
                String rateStr = etRate.getText().toString().trim();
                String budgetStr = etBudget.getText().toString().trim();
                String kwhStr = etKwh.getText().toString().trim();

                // Validate and Parse
                double rate = rateStr.isEmpty() ? 0.0 : Double.parseDouble(rateStr);
                double budget = budgetStr.isEmpty() ? 0.0 : Double.parseDouble(budgetStr);
                double kwh = kwhStr.isEmpty() ? 0.0 : Double.parseDouble(kwhStr);

                // Apply to SharedViewModel (which triggers persistence and global UI updates)
                sharedViewModel.setElectricityRate(rate);
                sharedViewModel.setMonthlyBudgetGoal(budget);
                sharedViewModel.setMonthlyKwhGoal(kwh);

                Toast.makeText(getContext(), "Configuration Applied!", Toast.LENGTH_SHORT).show();

                // Clear focus from inputs
                etRate.clearFocus();
                etBudget.clearFocus();
                etKwh.clearFocus();

            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid Input: Please enter valid numbers", Toast.LENGTH_SHORT).show();
            }
        });
    }
}