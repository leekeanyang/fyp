package com.example.fyp;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface EcoDao {
    @Insert
    void insertRecord(EcoRecord record);

    @Query("SELECT * FROM eco_records ORDER BY timestamp DESC")
    List<EcoRecord> getAllRecords();
}
