package com.example.fyp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

import com.example.fyp.R;

import java.util.HashMap;
import java.util.Map;

public class EcoTrackFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ecotrack, container, false);

        Spinner spinnerTransport = view.findViewById(R.id.spinner_transport);
        EditText editDistance = view.findViewById(R.id.edit_distance);
        EditText editNights = view.findViewById(R.id.edit_nights);
        Spinner spinnerAccommodation = view.findViewById(R.id.spinner_accommodation);
        Spinner spinnerMeals = view.findViewById(R.id.spinner_meals);
        Button btnCalculate = view.findViewById(R.id.btn_calculate);
        TextView tvResult = view.findViewById(R.id.tv_result);
        TextView tvTips = view.findViewById(R.id.tv_tips);

        btnCalculate.setOnClickListener(v -> {
            String distanceStr = editDistance.getText().toString();
            String nightsStr = editNights.getText().toString();

            if (distanceStr.isEmpty() || nightsStr.isEmpty()) {
                Toast.makeText(getContext(), "Please enter distance and nights", Toast.LENGTH_SHORT).show();
                return;
            }

            double distance = Double.parseDouble(distanceStr);
            int nights = Integer.parseInt(nightsStr);

            if (distance <= 0 || nights < 0) {
                Toast.makeText(getContext(), "Invalid inputs", Toast.LENGTH_SHORT).show();
                return;
            }

            // Updated Transport factors (kg CO₂e/km) as of 2025
            Map<String, Double> transportFactors = new HashMap<>();
            transportFactors.put("Car (petrol, single rider)", 0.25); // Adjusted for single rider
            transportFactors.put("Bus", 0.09);
            transportFactors.put("Train", 0.04);
            transportFactors.put("Domestic flight", 0.25);
            transportFactors.put("International flight", 0.15);
            String transport = spinnerTransport.getSelectedItem().toString();
            double transportCo2 = distance * transportFactors.getOrDefault(transport, 0.0);

            // Updated Accommodation factors (kg CO₂e/night)
            Map<String, Double> accomFactors = new HashMap<>();
            accomFactors.put("Hotel", 40.0);
            accomFactors.put("Camping", 5.0); // Kept as is, reasonable estimate
            String accom = spinnerAccommodation.getSelectedItem().toString();
            double accomCo2 = nights * accomFactors.getOrDefault(accom, 0.0);

            // Updated Meal factors (kg CO₂e/day)
            Map<String, Double> mealFactors = new HashMap<>();
            mealFactors.put("Vegetarian", 2.5);
            mealFactors.put("Meat-based", 3.6);
            String meals = spinnerMeals.getSelectedItem().toString();
            int days = nights + 1;
            double mealCo2 = days * mealFactors.getOrDefault(meals, 0.0);

            double totalCo2 = transportCo2 + accomCo2 + mealCo2;

            tvResult.setText("Total CO₂: " + String.format("%.2f", totalCo2) + " kg");

            // Tips
            StringBuilder tips = new StringBuilder("Tips to reduce:\n");
            if (transport.equals("Car (petrol, single rider)") || transport.contains("flight")) {
                tips.append("- Switch to bus or train to cut transport emissions.\n");
            }
            if (accom.equals("Hotel")) {
                tips.append("- Choose camping for lower impact stays.\n");
            }
            if (meals.equals("Meat-based")) {
                tips.append("- Opt for vegetarian meals to halve food emissions.\n");
            }
            if (totalCo2 > 100) {
                tips.append("- Overall high: Consider shorter trips or group travel.");
            }
            tvTips.setText(tips.toString());
        });

        return view;
    }
}