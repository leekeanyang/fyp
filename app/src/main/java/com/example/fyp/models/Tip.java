package com.example.fyp.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Tip implements Parcelable {
    private String id;
    private String title;
    private String text;
    private String description;
    private String category;
    private String imageUrl;
    private String site;
    private boolean completed; // Track completion status
    private static final String DEFAULT_IMAGE = "ic_eco_tip"; // Default image if null

    // Default constructor
    public Tip() {
        this.completed = false;
    }

    public Tip(String id, String title, String text, String description,
               String category, String imageUrl, String site) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.description = description;
        this.category = category;
        this.imageUrl = imageUrl != null ? imageUrl : DEFAULT_IMAGE;
        this.site = site;
        this.completed = false;
    }

    // Overloaded constructor for minimal initialization
    public Tip(String id, String title, String text, String category) {
        this(id, title, text, null, category, null, null);
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getText() { return text; }
    public String getDescription() { return description != null ? description : ""; }
    public String getCategory() { return category; }
    public String getImageUrl() { return imageUrl != null ? imageUrl : DEFAULT_IMAGE; }
    public String getSite() { return site != null ? site : ""; }
    public boolean isCompleted() { return completed; }
    public String getLocation() { return getSite(); } // Alias for location compatibility

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setText(String text) { this.text = text; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(String category) { this.category = category; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl != null ? imageUrl : DEFAULT_IMAGE; }
    public void setSite(String site) { this.site = site; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tip tip = (Tip) o;
        return id != null && id.equals(tip.id);
    }

    // Parcelable implementation
    protected Tip(Parcel in) {
        id = in.readString();
        title = in.readString();
        text = in.readString();
        description = in.readString();
        category = in.readString();
        imageUrl = in.readString();
        site = in.readString();
        completed = in.readByte() != 0; // Read boolean as byte
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
        dest.writeByte((byte) (completed ? 1 : 0)); // Write boolean as byte
    }

    @Override
    public String toString() {
        return "Tip{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", text='" + text + '\'' +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", site='" + site + '\'' +
                ", completed=" + completed +
                '}';
    }
}