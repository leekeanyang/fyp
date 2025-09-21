package com.example.fyp.models;

public class EcoReport {
    public String id;
    public String issueType;
    public String description;
    public boolean photoAttached;
    public int status; // 1 = Pending, 2 = In Review, 3 = Resolved

    public EcoReport() {} // Needed for Firebase

    public EcoReport(String id, String issueType, String description, boolean photoAttached, int status) {
        this.id = id;
        this.issueType = issueType;
        this.description = description;
        this.photoAttached = photoAttached;
        this.status = status;
    }
}

