package com.crystelle.electrolife.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.crystelle.electrolife.model.TrackedAppliance;

import java.util.List;

@Dao
public interface ApplianceDao {

    @Query("SELECT * FROM tracked_appliances")
    List<TrackedAppliance> getAllTracked();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(TrackedAppliance appliance);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<TrackedAppliance> appliances);

    @Query("DELETE FROM tracked_appliances WHERE id = :id")
    void deleteById(String id);

    @Update
    void update(TrackedAppliance appliance);
}