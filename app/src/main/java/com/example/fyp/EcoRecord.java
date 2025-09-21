package com.example.fyp;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "eco_records")
public class EcoRecord {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public double totalCO2;
    public double transportCO2;
    public double accommodationCO2;
    public double mealsCO2;
    public long timestamp;

    public EcoRecord(double totalCO2, double transportCO2, double accommodationCO2, double mealsCO2, long timestamp) {
        this.totalCO2 = totalCO2;
        this.transportCO2 = transportCO2;
        this.accommodationCO2 = accommodationCO2;
        this.mealsCO2 = mealsCO2;
        this.timestamp = timestamp;
    }
}
