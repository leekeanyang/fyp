package com.example.fyp.models;

import com.google.firebase.firestore.GeoPoint;

import java.util.List;

public class Trail {
    private String name;
    private String difficulty;
    private String estimatedTime; // e.g., "2 hours"
    private List<GeoPoint> points;

    public Trail(String name, String difficulty, String estimatedTime, List<GeoPoint> points) {
        this.name = name;
        this.difficulty = difficulty;
        this.estimatedTime = estimatedTime;
        this.points = points;
    }

    public String getName() { return name; }
    public String getDifficulty() { return difficulty; }
    public String getEstimatedTime() { return estimatedTime; }
    public List<GeoPoint> getPoints() { return points; }
}
