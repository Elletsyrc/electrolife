package com.crystelle.electrolife;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.crystelle.electrolife.ui.calculator.CalculatorFragment;
import com.crystelle.electrolife.ui.dialogs.AddApplianceBottomSheet;
import com.crystelle.electrolife.ui.history.HistoryFragment;
import com.crystelle.electrolife.ui.onboarding.OnboardingActivity;
import com.crystelle.electrolife.ui.settings.SettingsFragment;
import com.crystelle.electrolife.ui.tips.TipsFragment;
import com.crystelle.electrolife.workers.EnergyMonitorWorker;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.concurrent.TimeUnit;

/**
 * REVISED: MainActivity for ElectroLife.
 * Purpose: Acts as the primary container for the application fragments and
 * serves as the gatekeeper for the onboarding process.
 * * Re-wiring Logic: Checks SharedPreferences for the 'is_first_run' flag
 * before inflating any UI components to ensure a seamless first-user experience.
 */
public class MainActivity extends AppCompatActivity {

    // Unique name for the preference file to maintain consistency across the app
    private static final String PREFS_NAME = "electrolife_settings";

    // Key used to track if the user has completed the initial setup wizard
    private static final String KEY_IS_FIRST_RUN = "is_first_run";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- GATEKEEPER LOGIC: Onboarding Friction Check ---

        // Accessing the local XML preference storage
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Retrieving the first run status; default is true if the key doesn't exist
        boolean isFirstRun = prefs.getBoolean(KEY_IS_FIRST_RUN, true);

        // If the flag is true, redirect the user immediately to the OnboardingActivity
        if (isFirstRun) {
            // Initializing the intent to launch the onboarding wizard
            Intent onboardingIntent = new Intent(this, OnboardingActivity.class);

            // Starting the activity
            startActivity(onboardingIntent);

            // Destroying MainActivity so it is removed from the back stack
            finish();

            // Aborting further execution of onCreate to prevent background UI inflation
            return;
        }

        // --- STANDARD INITIALIZATION SECTION ---

        // If the user has passed onboarding, proceed with the main layout inflation
        setContentView(R.layout.activity_main);

        // Task 1: Background Monitoring Service Initialization
        // We schedule the daily budget check logic here to ensure it runs every 24 hours
        initiateBackgroundMonitoring();

        // Binding the Bottom Navigation UI component
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Establishing the Default Fragment View (Calculator) on startup
        if (savedInstanceState == null) {
            loadInitialFragment();
        }

        // Setting up the Tab Selection Listener to manage fragment transitions
        if (bottomNav != null) {
            bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                    Fragment selectedFragment = null;
                    int itemId = item.getItemId();

                    // Logic to determine which fragment to display based on menu ID
                    if (itemId == R.id.nav_calculator) {
                        selectedFragment = new CalculatorFragment();
                    } else if (itemId == R.id.nav_history) {
                        // Routing to the Archive/History display
                        selectedFragment = new HistoryFragment();
                    } else if (itemId == R.id.nav_tips) {
                        selectedFragment = new TipsFragment();
                    } else if (itemId == R.id.nav_settings) {
                        selectedFragment = new SettingsFragment();
                    }

                    // Perform the fragment replacement transaction
                    if (selectedFragment != null) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.nav_host_fragment, selectedFragment)
                                .commit();
                    }
                    return true;
                }
            });
        }
    }

    /**
     * Helper method to load the CalculatorFragment as the entry-point dashboard.
     */
    private void loadInitialFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, new CalculatorFragment())
                .commit();
    }

    /**
     * Task 1 Implementation: Sets up the WorkManager queue.
     * Checks energy consumption versus budget goals once every 24-hour cycle.
     */
    private void initiateBackgroundMonitoring() {
        // Defining the periodic work request constraints
        PeriodicWorkRequest budgetCheckRequest =
                new PeriodicWorkRequest.Builder(EnergyMonitorWorker.class, 24, TimeUnit.HOURS)
                        .build();

        // Enqueuing the work with the KEEP policy to prevent redundant schedules
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "DailyBudgetCheckWork",
                ExistingPeriodicWorkPolicy.KEEP,
                budgetCheckRequest
        );
    }

    /**
     * Provides an interface for fragments to trigger the Add Appliance Bottom Sheet.
     * This ensures unified management of the appliance library UI.
     */
    public void showAddApplianceDialog() {
        AddApplianceBottomSheet bottomSheet = new AddApplianceBottomSheet();
        bottomSheet.show(getSupportFragmentManager(), "AddApplianceBottomSheet");
    }
}