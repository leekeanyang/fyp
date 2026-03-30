package com.example.fyp.models;

public class CheckInRecord {
    private String id;
    private String siteId;
    private String siteName;
    private String userId;
    private String userEmail;
    private String method;
    private String qrPayload;
    private long timestamp;

    public CheckInRecord() {
    }

    public CheckInRecord(String id,
                         String siteId,
                         String siteName,
                         String userId,
                         String userEmail,
                         String method,
                         String qrPayload,
                         long timestamp) {
        this.id = id;
        this.siteId = siteId;
        this.siteName = siteName;
        this.userId = userId;
        this.userEmail = userEmail;
        this.method = method;
        this.qrPayload = qrPayload;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getQrPayload() {
        return qrPayload;
    }

    public void setQrPayload(String qrPayload) {
        this.qrPayload = qrPayload;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
