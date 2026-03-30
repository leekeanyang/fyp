package com.example.fyp.models;

import org.osmdroid.util.GeoPoint;

public class Destination {
    private String name;
    private double latitude;
    private double longitude;
    private String description;
    private String highlight;
    private GeoPoint[] trailPoints;
    private int currentVisitorCount;
    private int maxCapacity;

    public Destination(String name, double lat, double lon, String description, String highlight, int currentVisitorCount, int maxCapacity) {
        this.name = name;
        this.latitude = lat;
        this.longitude = lon;
        this.description = description;
        this.highlight = highlight;
        this.currentVisitorCount = currentVisitorCount;
        this.maxCapacity = maxCapacity;
    }

    public String getName() { return name; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getDescription() { return description; }
    public String getHighlight() { return highlight; }
    public GeoPoint[] getTrailPoints() { return trailPoints; }
    public int getCurrentVisitorCount() { return currentVisitorCount; }
    public int getMaxCapacity() { return maxCapacity; }

    public String getCapacityStatus() {
        double ratio = (double) currentVisitorCount / maxCapacity;
        if (ratio < 0.5) return "Low (Safe)";
        if (ratio < 0.8) return "Moderate";
        return "High (Crowded)";
    }
}
