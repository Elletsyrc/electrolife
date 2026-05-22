package com.crystelle.electrolife.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// Ensure you import both the new Entity and the new DAO
import com.crystelle.electrolife.model.MonthlyHistory;
import com.crystelle.electrolife.model.TrackedAppliance;

/**
 * REVISED: AppDatabase acts as the main access point for the persisted data.
 * 1. Added MonthlyHistory to the entities array.
 * 2. Incremented version to 2 to trigger the schema update.
 */
@Database(entities = {TrackedAppliance.class, MonthlyHistory.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    // Existing DAO
    public abstract ApplianceDao applianceDao();

    /**
     * FIX: Declare the abstract method for the History DAO.
     * This resolves the "Cannot resolve method 'historyDao'" error.
     */
    public abstract HistoryDao historyDao();

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "electrolife_db")
                            // CAUTION: destructiveMigration will clear current data
                            // to create the new 'monthly_history' table.
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}