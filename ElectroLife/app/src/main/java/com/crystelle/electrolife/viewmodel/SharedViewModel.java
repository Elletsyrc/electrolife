package com.crystelle.electrolife.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.crystelle.electrolife.data.AppDatabase;
import com.crystelle.electrolife.data.ApplianceDao;
import com.crystelle.electrolife.data.HistoryDao;
import com.crystelle.electrolife.model.MonthlyHistory;
import com.crystelle.electrolife.model.TrackedAppliance;
import com.crystelle.electrolife.utils.NotificationHelper;
import com.crystelle.electrolife.utils.SmartRecommendationsUtil;
import com.google.gson.Gson; // Added Gson import for snapshotting

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * REVISED: SharedViewModel for ElectroLife.
 * Purpose: Centralized state management for energy calculations, persistence, and history.
 * ROBUSTNESS UPDATE: Added strictly validated setters for Rate and Goals to support
 * immediate synchronization following the Onboarding Activity completion.
 */
public class SharedViewModel extends AndroidViewModel {

    private static final String TAG = "SharedViewModel";
    private static final String PREFS_NAME = "electrolife_settings";
    private static final String KEY_ELECTRICITY_RATE = "electricity_rate";
    private static final String KEY_BUDGET_GOAL = "budget_goal";
    private static final String KEY_KWH_GOAL = "kwh_goal";

    // Observable data streams for the UI fragments
    private final MutableLiveData<List<TrackedAppliance>> trackedAppliances = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Double> electricityRate = new MutableLiveData<>(12.00);
    private final MutableLiveData<Double> monthlyBudgetGoal = new MutableLiveData<>(1500.0);
    private final MutableLiveData<Double> monthlyKwhGoal = new MutableLiveData<>(300.0);

    private final SharedPreferences sharedPreferences;
    private final ApplianceDao applianceDao;
    private final HistoryDao historyDao;
    private final ExecutorService executorService;

    public SharedViewModel(@NonNull Application application) {
        super(application);
        this.sharedPreferences = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.applianceDao = AppDatabase.getInstance(application).applianceDao();
        this.historyDao = AppDatabase.getInstance(application).historyDao();
        this.executorService = Executors.newSingleThreadExecutor();

        // Initialize the data from local storage upon creation
        loadFromPersistence();
    }

    /**
     * Loads saved user preferences and appliance data into memory.
     */
    private void loadFromPersistence() {
        executorService.execute(() -> {
            try {
                List<TrackedAppliance> savedList = applianceDao.getAllTracked();

                if (savedList != null) {
                    // --- Database Self-Healing Routine ---
                    // Removes records with missing nested objects to prevent NullPointerExceptions.
                    for (int i = savedList.size() - 1; i >= 0; i--) {
                        TrackedAppliance a = savedList.get(i);
                        if (a.getAppliance() == null || a.getAppliance().getName() == null) {
                            applianceDao.deleteById(a.getId());
                            savedList.remove(i);
                        }
                    }
                    trackedAppliances.postValue(savedList);
                    processVampireMonitoring(savedList);
                }

                // ========================================================
                // THE FIX: FORCE OVERWRITE THE OLD CACHED RATE
                // ========================================================
                float savedRate = sharedPreferences.getFloat(KEY_ELECTRICITY_RATE, 12.00f);

                // If the app finds the old buggy 0.12 rate in the phone's memory,
                // it immediately upgrades it to 12.00 and saves it.
                if (savedRate == 0.12f) {
                    savedRate = 12.00f;
                    persistSetting(KEY_ELECTRICITY_RATE, savedRate);
                }

                electricityRate.postValue((double) savedRate);
                // ========================================================

                monthlyBudgetGoal.postValue((double) sharedPreferences.getFloat(KEY_BUDGET_GOAL, 1500.0f));
                monthlyKwhGoal.postValue((double) sharedPreferences.getFloat(KEY_KWH_GOAL, 300.0f));

            } catch (Exception e) {
                Log.e(TAG, "Error loading persistent data: " + e.getMessage());
            }
        });
    }

    private void persistSetting(String key, double value) {
        sharedPreferences.edit().putFloat(key, (float) value).apply();
    }

    // --- Getters for Data Observation ---
    public LiveData<List<TrackedAppliance>> getTrackedAppliances() { return trackedAppliances; }
    public LiveData<Double> getElectricityRate() { return electricityRate; }
    public LiveData<Double> getMonthlyBudgetGoal() { return monthlyBudgetGoal; }
    public LiveData<Double> getMonthlyKwhGoal() { return monthlyKwhGoal; }
    public LiveData<List<MonthlyHistory>> getAllHistory() { return historyDao.getAllHistory(); }

    /**
     * ROBUST SETTER: Updates the electricity rate.
     * Called during Onboarding or Settings updates. Ensures value is non-negative.
     */
    public void setElectricityRate(double rate) {
        if (rate < 0) {
            Log.w(TAG, "Attempted to set negative electricity rate. Ignoring.");
            return;
        }

        // Update LiveData for immediate UI response
        electricityRate.setValue(rate);

        executorService.execute(() -> {
            // Commit to XML preferences
            persistSetting(KEY_ELECTRICITY_RATE, rate);

            // Recalculate all costs and recommendations based on the new rate
            List<TrackedAppliance> currentList = trackedAppliances.getValue();
            if (currentList != null) {
                processVampireMonitoring(currentList);
            }
        });
    }

    /**
     * ROBUST SETTER: Updates the monthly financial budget goal.
     * Triggers a check against current projected consumption.
     */
    public void setMonthlyBudgetGoal(double goal) {
        if (goal < 0) {
            Log.w(TAG, "Attempted to set negative budget goal. Ignoring.");
            return;
        }

        monthlyBudgetGoal.setValue(goal);

        executorService.execute(() -> {
            persistSetting(KEY_BUDGET_GOAL, goal);

            List<TrackedAppliance> currentList = trackedAppliances.getValue();
            if (currentList != null) {
                processVampireMonitoring(currentList);
            }
        });
    }

    /**
     * ROBUST SETTER: Updates the monthly energy consumption goal (kWh).
     */
    public void setMonthlyKwhGoal(double goal) {
        if (goal < 0) {
            Log.w(TAG, "Attempted to set negative energy goal. Ignoring.");
            return;
        }

        monthlyKwhGoal.setValue(goal);

        executorService.execute(() -> {
            persistSetting(KEY_KWH_GOAL, goal);

            List<TrackedAppliance> currentList = trackedAppliances.getValue();
            if (currentList != null) {
                processVampireMonitoring(currentList);
            }
        });
    }

    /**
     * Core logic for background analytics, budget tracking, and smart notifications.
     */
    private void processVampireMonitoring(List<TrackedAppliance> list) {
        if (list == null || list.isEmpty()) return;

        // Retrieve current thresholds, defaulting to safe values if null
        double rate = electricityRate.getValue() != null ? electricityRate.getValue() : 12.00;
        double budget = monthlyBudgetGoal.getValue() != null ? monthlyBudgetGoal.getValue() : 0;
        double limit = monthlyKwhGoal.getValue() != null ? monthlyKwhGoal.getValue() : 0;

        // Generate context-aware recommendations
        SmartRecommendationsUtil.generateSmartRecommendations(list, rate, budget, limit);

        double totalMonthlyCost = 0;
        double totalMonthlyKwh = 0;

        // Aggregate usage across all tracked devices
        for (TrackedAppliance a : list) {
            if (a.getAppliance() != null) {
                double monthlyKwh = (a.getAppliance().getDefaultWattage() / 1000.0) * a.getHoursPerDay() * 30.0;
                totalMonthlyKwh += monthlyKwh;
                totalMonthlyCost += (monthlyKwh * rate);
            }
        }

        // Trigger system notifications if user-defined goals are breached
        if (budget > 0 && totalMonthlyCost > budget) {
            NotificationHelper.showBudgetNotification(getApplication(), "Budget Limit Reached",
                    String.format(Locale.getDefault(), "Projected cost (₱%.2f) exceeded goal.", totalMonthlyCost));
        }

        if (limit > 0 && totalMonthlyKwh > limit) {
            NotificationHelper.showBudgetNotification(getApplication(), "Energy Goal Warning",
                    String.format(Locale.getDefault(), "Usage is %.1f kWh. Surpassed limit.", totalMonthlyKwh));
        }

        // Post the processed list back to UI listeners
        trackedAppliances.postValue(new ArrayList<>(list));

        // Sync the state changes back to the Room database
        executorService.execute(() -> applianceDao.insertAll(list));
    }

    /**
     * Captures a snapshot of the current month's usage and archives it in history.
     */
    public void archiveMonthlyHistory(String monthName, int year) {
        // Grab the LiveData values BEFORE going into the background thread to avoid threading violations
        final List<TrackedAppliance> currentAppliances = trackedAppliances.getValue();
        final Double currentRate = electricityRate.getValue();
        final Double currentBudget = monthlyBudgetGoal.getValue();
        final Double currentKwhGoal = monthlyKwhGoal.getValue();

        executorService.execute(() -> {
            if (currentAppliances != null && !currentAppliances.isEmpty() && currentRate != null) {
                double calculatedTotalKwh = 0;
                double calculatedTotalCost = 0;

                for (TrackedAppliance a : currentAppliances) {
                    if (a.getAppliance() == null) continue;
                    double appMonthlyKwh = (a.getAppliance().getDefaultWattage() / 1000.0) * a.getHoursPerDay() * 30.0;
                    calculatedTotalKwh += appMonthlyKwh;
                    calculatedTotalCost += (appMonthlyKwh * currentRate);
                }

                MonthlyHistory record = new MonthlyHistory(monthName, year, calculatedTotalKwh, calculatedTotalCost);
                if (currentBudget != null) record.setBudgetGoal(currentBudget);
                if (currentKwhGoal != null) record.setKwhGoal(currentKwhGoal);

                // NEW: Convert the current appliance list to a JSON string and save it to history
                // This acts as a frozen snapshot of exact appliance states
                String jsonSnapshot = new Gson().toJson(currentAppliances);
                record.setApplianceSnapshotJson(jsonSnapshot);

                historyDao.insertHistory(record);
            }
        });
    }

    // --- Standard Database Mutators ---

    public void addAppliance(TrackedAppliance appliance) {
        List<TrackedAppliance> currentList = trackedAppliances.getValue();
        List<TrackedAppliance> updatedList = currentList != null ? new ArrayList<>(currentList) : new ArrayList<>();
        updatedList.add(appliance);
        trackedAppliances.setValue(updatedList);
        executorService.execute(() -> {
            applianceDao.upsert(appliance);
            processVampireMonitoring(updatedList);
        });
    }

    public void removeAppliance(String id) {
        executorService.execute(() -> {
            applianceDao.deleteById(id);
            List<TrackedAppliance> updatedList = applianceDao.getAllTracked();
            updatedList.removeIf(a -> a.getAppliance() == null);
            trackedAppliances.postValue(updatedList);
            processVampireMonitoring(updatedList);
        });
    }

    public void updateApplianceHours(String id, double hours) {
        executorService.execute(() -> {
            List<TrackedAppliance> currentList = applianceDao.getAllTracked();
            if (currentList == null) return;
            for (TrackedAppliance a : currentList) {
                if (a.getId().equals(id)) {
                    a.setHoursPerDay(hours);
                    applianceDao.upsert(a);
                    break;
                }
            }
            currentList.removeIf(a -> a.getAppliance() == null);
            trackedAppliances.postValue(currentList);
            processVampireMonitoring(currentList);
        });
    }

    public void updateApplianceLimit(String id, double limit) {
        executorService.execute(() -> {
            List<TrackedAppliance> currentList = applianceDao.getAllTracked();
            if (currentList == null) return;
            for (TrackedAppliance a : currentList) {
                if (a.getId().equals(id)) {
                    a.setCustomKwhLimit(limit);
                    applianceDao.upsert(a);
                    break;
                }
            }
            currentList.removeIf(a -> a.getAppliance() == null);
            trackedAppliances.postValue(currentList);
            processVampireMonitoring(currentList);
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Shutdown the executor service to prevent memory leaks
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}