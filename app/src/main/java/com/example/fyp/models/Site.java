package com.example.fyp.models;

public class Site {
    private String name;
    private String description;
    private int imageResId;

    public Site(String name, String description, int imageResId) {
        this.name = name;
        this.description = description;
        this.imageResId = imageResId;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getImageResId() { return imageResId; }

    public int getLongDesc() {
        return 0;
    }

    public boolean getLat() {
        return false;
    }

    public boolean getLng() {
        return false;
    }
}
