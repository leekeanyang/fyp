package com.example.fyp;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Trip {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String transport;
    public double distance;
    public double carbon;
    public long timestamp;
}

