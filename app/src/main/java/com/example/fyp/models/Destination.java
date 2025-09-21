package com.example.fyp.models;

import org.osmdroid.util.GeoPoint;

public class Destination {
    private String name;
    private double latitude;
    private double longitude;
    private String description;
    private String highlight;
    private GeoPoint[] trailPoints; // Optional for trail paths

    public Destination(String name, double lat, double lon, String description, String highlight) {
        this.name = name;
        this.latitude = lat;
        this.longitude = lon;
        this.description = description;
        this.highlight = highlight;
    }

    // Optional constructor for trail paths
    public Destination(String name, double lat, double lon, String description, String highlight, GeoPoint[] trailPoints) {
        this.name = name;
        this.latitude = lat;
        this.longitude = lon;
        this.description = description;
        this.highlight = highlight;
        this.trailPoints = trailPoints;
    }

    public String getName() { return name; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getDescription() { return description; }
    public String getHighlight() { return highlight; }
    public GeoPoint[] getTrailPoints() { return trailPoints; }
}
