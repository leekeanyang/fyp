package com.example.fyp.utils;

public class CarbonCalculator {

    public static double calculateFootprint(String transport, String accommodation, String meal) {
        double transportValue = getTransportValue(transport);
        double accommodationValue = getAccommodationValue(accommodation);
        double mealValue = getMealValue(meal);

        return transportValue + accommodationValue + mealValue;
    }

    private static double getTransportValue(String mode) {
        switch (mode) {
            case "Car": return 5.0;
            case "Bus": return 2.0;
            case "Train": return 1.5;
            case "Bicycle/Walk": return 0.0;
            default: return 0.0;
        }
    }

    private static double getAccommodationValue(String type) {
        switch (type) {
            case "Luxury Hotel": return 4.0;
            case "Eco-Lodge": return 1.5;
            case "Camping": return 0.5;
            default: return 0.0;
        }
    }

    private static double getMealValue(String meal) {
        switch (meal) {
            case "Meat-heavy": return 4.0;
            case "Mixed Diet": return 2.5;
            case "Vegetarian/Vegan": return 1.0;
            default: return 0.0;
        }
    }
}
