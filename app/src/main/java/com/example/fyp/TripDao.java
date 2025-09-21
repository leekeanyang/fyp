package com.example.fyp;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TripDao {
    @Insert
    void insert(Trip trip);

    @Query("SELECT * FROM Trip ORDER BY timestamp DESC")
    List<Trip> getAllTrips();
}

