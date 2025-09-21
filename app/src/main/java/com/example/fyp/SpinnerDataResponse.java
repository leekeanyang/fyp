package com.example.fyp;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SpinnerDataResponse {
    @SerializedName("transport_modes")
    List<String> transportModes;

    @SerializedName("accommodation_types")
    List<String> accommodationTypes;

    @SerializedName("meal_preferences")
    List<String> mealPreferences;

    // Getters and setters...
}

