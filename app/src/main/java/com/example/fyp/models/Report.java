package com.example.fyp.models;

import java.io.Serializable;

public class Report implements Serializable {
    private String id;
    private String siteId;
    private String site;
    private String issueType;
    private String description;
    private String photoUrl;
    private String status;
    private String location;
    private double latitude;
    private double longitude;
    private String submittedByUserId;
    private String submittedByEmail;
    private String adminAction;
    private String adminName;
    private long updatedAt;
    private long timestamp;

    public Report() {
        // Default constructor for Firestore
    }

    public Report(String id, String site, String issueType, String description, String photoUrl, String status, String location, long timestamp) {
        this(id, "", site, issueType, description, photoUrl, status, location, 0.0, 0.0, "", "", "", "", timestamp, timestamp);
    }

    public Report(String id,
                  String siteId,
                  String site,
                  String issueType,
                  String description,
                  String photoUrl,
                  String status,
                  String location,
                  double latitude,
                  double longitude,
                  String submittedByUserId,
                  String submittedByEmail,
                  String adminAction,
                  String adminName,
                  long updatedAt,
                  long timestamp) {
        this.id = id;
        this.siteId = siteId;
        this.site = site;
        this.issueType = issueType;
        this.description = description;
        this.photoUrl = photoUrl;
        this.status = status;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.submittedByUserId = submittedByUserId;
        this.submittedByEmail = submittedByEmail;
        this.adminAction = adminAction;
        this.adminName = adminName;
        this.updatedAt = updatedAt;
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

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
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

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getSubmittedByUserId() {
        return submittedByUserId;
    }

    public void setSubmittedByUserId(String submittedByUserId) {
        this.submittedByUserId = submittedByUserId;
    }

    public String getSubmittedByEmail() {
        return submittedByEmail;
    }

    public void setSubmittedByEmail(String submittedByEmail) {
        this.submittedByEmail = submittedByEmail;
    }

    public String getAdminAction() {
        return adminAction;
    }

    public void setAdminAction(String adminAction) {
        this.adminAction = adminAction;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatusStep() {
        if ("Resolved".equalsIgnoreCase(status)) {
            return 3;
        }
        if ("Reviewed".equalsIgnoreCase(status)) {
            return 2;
        }
        return 1;
    }
}
