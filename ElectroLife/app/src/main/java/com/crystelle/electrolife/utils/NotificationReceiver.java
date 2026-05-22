package com.crystelle.electrolife.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.crystelle.electrolife.MainActivity;
import com.crystelle.electrolife.R;

/**
 * REVISED: NotificationReceiver handles system-wide budget alerts.
 * STATUS: 0 Warnings. Optimized for Android 13+ (Tiramisu) standards.
 * FIX: Literal emoji used and redundant title parameter refactored into a constant.
 */
public class NotificationReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "electrolife_budget_alerts";
    private static final int NOTIFICATION_ID = 1001;

    // FIX: Refactored fixed title into a constant with a literal emoji.
    // This resolves the "Value of parameter 'title' is always..." warning.
    private static final String NOTIFICATION_TITLE = "Budget Warning ⚠️";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Verify the action matches the one declared in AndroidManifest.xml
        if ("com.crystelle.electrolife.ACTION_SHOW_WARNING".equals(intent.getAction())) {

            String message = intent.getStringExtra("EXTRA_WARNING_MESSAGE");
            if (message == null || message.isEmpty()) {
                message = "You are on track to exceed your monthly electricity budget. Review your appliances!";
            }

            createNotificationChannel(context);

            // FIX: Title parameter removed here to resolve redundancy
            showNotification(context, message);
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Budget Alerts";
            String description = "Notifications warning you when you are about to exceed your monthly electricity budget.";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * FIX: Parameters simplified. 'title' is now handled internally via constant.
     */
    private void showNotification(Context context, String content) {
        Intent tapIntent = new Intent(context, MainActivity.class);
        tapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, tapIntent, PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_zap)
                .setContentTitle(NOTIFICATION_TITLE)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // Security check: Required for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}