package com.example.fyp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;

import com.example.fyp.R;

public class HomeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Add click listeners for placeholders (e.g., start new activities)
        Button btnSustainability = view.findViewById(R.id.btn_sustainability);
        btnSustainability.setOnClickListener(v -> {
            // TODO: Launch SustainabilityTipsActivity
        });

        return view;
    }
}