package com.crystelle.electrolife.workers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.crystelle.electrolife.R;
import com.crystelle.electrolife.data.AppDatabase;
import com.crystelle.electrolife.model.TrackedAppliance;
import com.crystelle.electrolife.utils.CalculationsUtil;

import java.util.List;

public class EnergyMonitorWorker extends Worker {

    // Notification Channel constants for Android O+ compatibility
    private static final String CHANNEL_ID = "energy_alerts_channel";
    private static final String PREFS_NAME = "electrolife_settings";

    public EnergyMonitorWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        // 1. Retrieve Data: Access Room Database directly (WorkManager runs on a background thread)
        AppDatabase db = AppDatabase.getInstance(context);
        List<TrackedAppliance> appliances = db.applianceDao().getAllTracked();

        // 2. Retrieve Goals: Get the current rate and budget from SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        double rate = prefs.getFloat("electricity_rate", 0.12f);
        double budgetGoal = prefs.getFloat("budget_goal", 100.0f);

        // 3. Calculation Logic: Determine the projected monthly cost
        if (appliances != null && !appliances.isEmpty()) {
            double totalMonthlyCost = CalculationsUtil.calculateTotalMonthlyCost(appliances, rate);

            // 4. Comparison: Check if the current usage exceeds the set budget goal
            if (totalMonthlyCost > budgetGoal) {
                triggerLimitNotification(context, totalMonthlyCost, budgetGoal);
            }
        }

        // Return success to tell WorkManager the task finished correctly
        return Result.success();
    }

    /**
     * Helper to build and display a system notification when energy limits are hit.
     */
    private void triggerLimitNotification(Context context, double current, double goal) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create the Notification Channel for API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Energy Budget Alerts",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        String message = String.format("Alert: Your projected monthly cost of %s has exceeded your %s budget!",
                CalculationsUtil.formatCurrency(current), CalculationsUtil.formatCurrency(goal));

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_bolt) // Ensure this drawable exists
                .setContentTitle("Energy Limit Exceeded")
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        notificationManager.notify(1001, builder.build());
    }
}