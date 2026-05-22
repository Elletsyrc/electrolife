package com.crystelle.electrolife.ui.onboarding;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.crystelle.electrolife.MainActivity;
import com.crystelle.electrolife.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * REVISED: Onboarding Wizard Logic for ElectroLife.
 * Purpose: Manages a three-stage user introduction journey.
 * * JOURNEY STAGES:
 * 1. SPLASH: A 5-second animated logo introduction.
 * 2. MARKETING: A swipeable series of cards detailing app benefits (using ViewPager2).
 * 3. SETUP: Final baseline configuration for rates and goals.
 */
public class OnboardingActivity extends AppCompatActivity {

    // --- PERSISTENCE CONSTANTS ---
    private static final String PREFS_NAME = "electrolife_settings";
    private static final String KEY_ELECTRICITY_RATE = "electricity_rate";
    private static final String KEY_BUDGET_GOAL = "budget_goal";
    private static final String KEY_KWH_GOAL = "kwh_goal";
    private static final String KEY_IS_FIRST_RUN = "is_first_run";

    // --- STAGE CONSTANTS ---
    private static final int STAGE_SPLASH = 0;
    private static final int STAGE_MARKETING = 1;
    private static final int STAGE_SETUP = 2;
    private static final int SPLASH_DELAY_MS = 5000; // 5-second timer
    private static final int TOTAL_MARKETING_CARDS = 5;

    // --- UI COMPONENTS ---
    private ViewFlipper mainStageFlipper;

    // NEW: ViewPager2 and TabLayout replacing the old marketingCardFlipper
    private ViewPager2 viewPagerOnboarding;
    private TabLayout tabLayoutIndicators;
    private OnboardingPagerAdapter pagerAdapter;

    private TextInputEditText etRate, etBudget, etKwhGoal;
    private TextInputLayout tilRate, tilBudget, tilKwhGoal;
    private Button btnNextMarketing, btnFinishSetup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        // --- UI INITIALIZATION ---
        initializeUserInterface();

        // --- VIEWPAGER2 SETUP ---
        setupSwipeableOnboarding();

        // --- STAGE 1: AUTOMATED SPLASH LOGIC ---
        // We use a Handler with the Main Looper to trigger the transition after 5 seconds.
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Transitions from the Logo Splash to the Marketing Swipe Cards
            if (mainStageFlipper != null) {
                mainStageFlipper.setDisplayedChild(STAGE_MARKETING);
            }
        }, SPLASH_DELAY_MS);
    }

    /**
     * Binds all XML views to Java objects and sets up event listeners.
     */
    private void initializeUserInterface() {
        // Stage Controllers
        mainStageFlipper = findViewById(R.id.onboarding_main_flipper);

        // NEW: Binding the swipeable container and dot indicators
        viewPagerOnboarding = findViewById(R.id.view_pager_onboarding);
        tabLayoutIndicators = findViewById(R.id.tab_layout_indicators);

        // Marketing Navigation (Now acts as the bridge to STAGE_SETUP)
        btnNextMarketing = findViewById(R.id.btn_marketing_next);
        if (btnNextMarketing != null) {
            btnNextMarketing.setOnClickListener(v -> transitionToSetupStage());
        }

        // Configuration Inputs
        etRate = findViewById(R.id.et_onboarding_rate);
        etBudget = findViewById(R.id.et_onboarding_budget);
        etKwhGoal = findViewById(R.id.et_onboarding_kwh_goal);

        // Layout Containers for Error States
        tilRate = findViewById(R.id.til_onboarding_rate);
        tilBudget = findViewById(R.id.til_onboarding_budget);
        tilKwhGoal = findViewById(R.id.til_onboarding_kwh_goal);

        // Final Action Button
        btnFinishSetup = findViewById(R.id.btn_get_started);
        if (btnFinishSetup != null) {
            btnFinishSetup.setOnClickListener(v -> handleFinalAction());
        }
    }

    /**
     * STAGE 2 LOGIC: Sets up the ViewPager2 adapter and TabLayoutMediator for swipe gestures.
     */
    private void setupSwipeableOnboarding() {
        if (viewPagerOnboarding == null || tabLayoutIndicators == null) return;

        // Initialize the custom adapter for the slides
        pagerAdapter = new OnboardingPagerAdapter(this);
        viewPagerOnboarding.setAdapter(pagerAdapter);

        // Attach TabLayout to ViewPager2 to sync the dot indicators
        new TabLayoutMediator(tabLayoutIndicators, viewPagerOnboarding,
                (tab, position) -> {
                    // Empty implementation: Dots are styled via XML tabBackground
                }).attach();

        // Register callback to hide/show the Continue button based on swipe position
        viewPagerOnboarding.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == TOTAL_MARKETING_CARDS - 1) {
                    // Reveal the button when the user reaches the final slide
                    btnNextMarketing.setVisibility(View.VISIBLE);
                } else {
                    // Hide the button if they swipe backward
                    btnNextMarketing.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * Transitions from the Marketing Swipe Stage to the Final Setup Stage.
     */
    private void transitionToSetupStage() {
        if (mainStageFlipper != null) {
            mainStageFlipper.setDisplayedChild(STAGE_SETUP);
        }
    }

    /**
     * STAGE 3 LOGIC: Finalizes the onboarding by validating inputs and persisting data.
     */
    private void handleFinalAction() {
        boolean isDataValid = validateAndSave();

        if (isDataValid) {
            transitionToDashboard();
        }
    }

    /**
     * core validation logic to parse user inputs and commit them to SharedPreferences.
     */
    private boolean validateAndSave() {
        String rateStr = etRate.getText() != null ? etRate.getText().toString().trim() : "";
        String budgetStr = etBudget.getText() != null ? etBudget.getText().toString().trim() : "";
        String kwhStr = etKwhGoal.getText() != null ? etKwhGoal.getText().toString().trim() : "";

        boolean isValid = true;

        // Reset UI Error States
        if (tilRate != null) tilRate.setError(null);
        if (tilBudget != null) tilBudget.setError(null);
        if (tilKwhGoal != null) tilKwhGoal.setError(null);

        float rate = 0, budget = 0, kwhGoal = 0;

        try {
            // Validation 1: Rate
            if (rateStr.isEmpty()) {
                tilRate.setError("Rate is required");
                isValid = false;
            } else {
                rate = Float.parseFloat(rateStr);
                if (rate <= 0) { tilRate.setError("Must be > 0"); isValid = false; }
            }

            // Validation 2: Budget
            if (budgetStr.isEmpty()) {
                tilBudget.setError("Budget is required");
                isValid = false;
            } else {
                budget = Float.parseFloat(budgetStr);
                if (budget <= 0) { tilBudget.setError("Must be > 0"); isValid = false; }
            }

            // Validation 3: kWh Goal
            if (kwhStr.isEmpty()) {
                tilKwhGoal.setError("Goal is required");
                isValid = false;
            } else {
                kwhGoal = Float.parseFloat(kwhStr);
                if (kwhGoal <= 0) { tilKwhGoal.setError("Must be > 0"); isValid = false; }
            }
        } catch (NumberFormatException e) {
            isValid = false;
            Toast.makeText(this, "Please enter valid numeric values", Toast.LENGTH_SHORT).show();
        }

        if (!isValid) return false;

        // Committing validated user preferences to XML persistence
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putFloat(KEY_ELECTRICITY_RATE, rate);
        editor.putFloat(KEY_BUDGET_GOAL, budget);
        editor.putFloat(KEY_KWH_GOAL, kwhGoal);
        editor.putBoolean(KEY_IS_FIRST_RUN, false); // Mark journey as complete

        editor.apply();

        Toast.makeText(this, "Welcome to ElectroLife!", Toast.LENGTH_SHORT).show();
        return true;
    }

    /**
     * Executes the Intent transition to the MainActivity and clears the activity stack.
     */
    private void transitionToDashboard() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}