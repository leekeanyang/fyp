package com.example.fyp.models;

import java.util.Date;

public class AdminLog {
    private String userId;
    private String changedBy;
    private String newRole;
    private Date timestamp;

    public AdminLog() {}

    public String getUserId() { return userId; }
    public String getChangedBy() { return changedBy; }
    public String getNewRole() { return newRole; }
    public Date getTimestamp() { return timestamp; }
}
