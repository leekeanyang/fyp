package com.example.fyp.models;

import java.io.Serializable;

public class Site implements Serializable {
    private String name;
    private String shortDescription;
    private String longDescription;
    private String highlights;
    private String guidelines;
    private String warnings;
    private int imageResId;
    private double lat;
    private double lng;

    public Site(String name, String shortDesc, int imageResId) {
        this.name = name;
        this.shortDescription = shortDesc;
        this.imageResId = imageResId;
        this.longDescription = "";
        this.highlights = "";
        this.guidelines = "";
        this.warnings = "";
        this.lat = 0.0;
        this.lng = 0.0;
    }

    public Site(String name, String shortDesc, String longDesc, String highlights, String guidelines, String warnings, int imageResId, double lat, double lng) {
        this.name = name;
        this.shortDescription = shortDesc;
        this.longDescription = longDesc;
        this.highlights = highlights;
        this.guidelines = guidelines;
        this.warnings = warnings;
        this.imageResId = imageResId;
        this.lat = lat;
        this.lng = lng;
    }

    public String getName() { return name; }
    public String getShortDescription() { return shortDescription; }
    public String getLongDescription() { return longDescription; }
    public String getDescription() { return shortDescription; }
    public String getHighlights() { return highlights; }
    public String getGuidelines() { return guidelines; }
    public String getWarnings() { return warnings; }
    public int getImageResId() { return imageResId; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }
}
