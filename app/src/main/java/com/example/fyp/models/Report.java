package com.example.fyp.models;

import java.io.Serializable;

public class Report implements Serializable {
    private String id;
    private String site;
    private String issueType;
    private String description;
    private String photoUrl;
    private String status;
    private String location;
    private long timestamp;

    public Report() {
        // Default constructor for Firestore
    }

    public Report(String id, String site, String issueType, String description, String photoUrl, String status, String location, long timestamp) {
        this.id = id;
        this.site = site;
        this.issueType = issueType;
        this.description = description;
        this.photoUrl = photoUrl;
        this.status = status;
        this.location = location;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}