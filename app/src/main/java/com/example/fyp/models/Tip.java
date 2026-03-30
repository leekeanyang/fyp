package com.example.fyp.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

public class Tip implements Parcelable {
    private String id;
    private String title;
    private String text;
    private String description;
    private String category;
    private String imageUrl;
    private String site;
    private double co2Savings; // New field for numerical savings
    private boolean completed;
    private static final String DEFAULT_IMAGE = "ic_eco_tip";

    public Tip() {
        this.completed = false;
    }

    public Tip(String id, String title, String text, String description,
               String category, String imageUrl, String site, double co2Savings) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.description = description;
        this.category = category;
        this.imageUrl = imageUrl != null ? imageUrl : DEFAULT_IMAGE;
        this.site = site;
        this.co2Savings = co2Savings;
        this.completed = false;
    }

    // Overloaded constructor for legacy support
    public Tip(String id, String title, String text, String description,
               String category, String imageUrl, String site) {
        this(id, title, text, description, category, imageUrl, site, 0.0);
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getText() { return text; }
    public String getDescription() { return description != null ? description : ""; }
    public String getCategory() { return category; }
    public String getImageUrl() { return imageUrl != null ? imageUrl : DEFAULT_IMAGE; }
    public String getSite() { return site != null ? site : ""; }
    public double getCo2Savings() { return co2Savings; }
    public boolean isCompleted() { return completed; }
    public String getLocation() { return getSite(); }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setText(String text) { this.text = text; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(String category) { this.category = category; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl != null ? imageUrl : DEFAULT_IMAGE; }
    public void setSite(String site) { this.site = site; }
    public void setCo2Savings(double co2Savings) { this.co2Savings = co2Savings; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tip tip = (Tip) o;
        return Double.compare(tip.co2Savings, co2Savings) == 0 &&
                completed == tip.completed &&
                Objects.equals(id, tip.id) &&
                Objects.equals(title, tip.title) &&
                Objects.equals(text, tip.text) &&
                Objects.equals(description, tip.description) &&
                Objects.equals(category, tip.category) &&
                Objects.equals(imageUrl, tip.imageUrl) &&
                Objects.equals(site, tip.site);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, text, description, category, imageUrl, site, co2Savings, completed);
    }

    protected Tip(Parcel in) {
        id = in.readString();
        title = in.readString();
        text = in.readString();
        description = in.readString();
        category = in.readString();
        imageUrl = in.readString();
        site = in.readString();
        co2Savings = in.readDouble();
        completed = in.readByte() != 0;
    }

    public static final Creator<Tip> CREATOR = new Creator<Tip>() {
        @Override
        public Tip createFromParcel(Parcel in) {
            return new Tip(in);
        }

        @Override
        public Tip[] newArray(int size) {
            return new Tip[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(text);
        dest.writeString(description);
        dest.writeString(category);
        dest.writeString(imageUrl);
        dest.writeString(site);
        dest.writeDouble(co2Savings);
        dest.writeByte((byte) (completed ? 1 : 0));
    }
}
