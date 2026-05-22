package com.crystelle.electrolife.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.crystelle.electrolife.MainActivity;
import com.crystelle.electrolife.R;

/**
 * Task 1: Background Monitoring Utility
 * Purpose: Centralized helper to manage Notification Channels and
 * construct high-priority visual push notifications for budget alerts.
 * STATUS: Method now called from SharedViewModel.
 */
public class NotificationHelper {

    public static final String CHANNEL_ID = "budget_alerts_channel";
    public static final String CHANNEL_NAME = "Budget & Energy Alerts";
    public static final String CHANNEL_DESC = "Notifications for energy budget limit breaches";
    public static final int NOTIFICATION_ID = 1001;

    /**
     * Builds and displays a budget limit notification.
     * @param context App context (Use getApplication() from ViewModel)
     * @param title   The heading of the notification
     * @param message The detailed body text
     */
    public static void showBudgetNotification(Context context, String title, String message) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // 1. Create the Notification Channel (Required for API 26+)
        createNotificationChannel(context, notificationManager);

        // 2. Setup the Tap Action
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // FLAG_IMMUTABLE is required for modern Android security
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 3. Construct the Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_bolt)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(context.getColor(R.color.blue_600))
                .setDefaults(NotificationCompat.DEFAULT_ALL); // Sound, Vibrate, Lights

        // 4. Fire the Notification
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    private static void createNotificationChannel(Context context, NotificationManager manager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (manager != null && manager.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription(CHANNEL_DESC);
                channel.enableLights(true);
                channel.setLightColor(context.getColor(R.color.blue_600));
                channel.enableVibration(true);

                manager.createNotificationChannel(channel);
            }
        }
    }
}