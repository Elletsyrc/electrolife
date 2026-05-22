package com.crystelle.electrolife.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.crystelle.electrolife.model.MonthlyHistory;

import java.util.List;

@Dao
public interface HistoryDao {

    // CRITICAL FIX: OnConflictStrategy.REPLACE stops the app from crashing
    // if you accidentally archive the same month twice. It just updates the old one!
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertHistory(MonthlyHistory history);

    @Query("SELECT * FROM monthly_history ORDER BY id DESC")
    LiveData<List<MonthlyHistory>> getAllHistory();
}