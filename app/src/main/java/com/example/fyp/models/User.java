package com.example.fyp.models;

public class User {
    private String uid;
    private String email;
    private String role;

    public User() {}  // Firestore needs empty constructor

    public User(String uid, String email, String role) {
        this.uid = uid;
        this.email = email;
        this.role = role;
    }

    public String getUid() { return uid; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}

