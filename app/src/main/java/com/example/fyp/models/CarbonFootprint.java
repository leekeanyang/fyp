package com.example.fyp.models;

import java.util.HashMap;
import java.util.Map;

public class CarbonFootprint {

    private static final Map<String, Double> transportFactors = new HashMap<>();
    private static final Map<String, Double> accommodationFactors = new HashMap<>();
    private static final Map<String, Double> mealFactors = new HashMap<>();

    static {
        // Transport factors (aligned with transport_modes array)
        transportFactors.put("Car (single rider)", 0.225); // kg/km
        transportFactors.put("Bus", 0.075);
        transportFactors.put("Train", 0.031);
        transportFactors.put("Domestic Flight", 0.275);
        transportFactors.put("International Flight", 0.175);

        // Accommodation factors (aligned with accommodation_types array)
        accommodationFactors.put("Hotel", 20.0); // kg/night
        accommodationFactors.put("Eco-Lodge", 10.0);
        accommodationFactors.put("Guesthouse", 12.0);
        accommodationFactors.put("Hostel", 8.0);
        accommodationFactors.put("Resort", 25.0);
        accommodationFactors.put("Camping", 5.0);

        // Meal factors (aligned with meal_preferences array)
        mealFactors.put("Vegetarian", 2.5); // kg/day
        mealFactors.put("Vegan", 1.5);
        mealFactors.put("Halal", 4.0); // Assuming meat-based, similar to original Meat-based
        mealFactors.put("Gluten-Free", 3.0); // Moderate impact, assuming mixed diet
        mealFactors.put("No Preference", 3.5); // Average impact
    }

    public static double getTransportFactor(String transport) {
        return transportFactors.getOrDefault(transport, 0.0);
    }

    public static double getAccommodationFactor(String accommodation) {
        return accommodationFactors.getOrDefault(accommodation, 0.0);
    }

    public static double getMealFactor(String meal) {
        return mealFactors.getOrDefault(meal, 0.0);
    }
}