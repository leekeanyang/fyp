package com.example.fyp.models;
public class UserProfile {
    private String uid;
    private String name;
    private String email;
    private String phone;
    private String bio;
    private String location;
    private String photoUrl;

    public UserProfile() {} // Required for Firestore

    public UserProfile(String uid, String name, String email, String phone, String bio, String location, String photoUrl) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.bio = bio;
        this.location = location;
        this.photoUrl = photoUrl;
    }

    // Getters and setters
    public String getUid() { return uid; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getBio() { return bio; }
    public String getLocation() { return location; }
    public String getPhotoUrl() { return photoUrl; }
}

