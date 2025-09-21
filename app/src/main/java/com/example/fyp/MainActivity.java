package com.example.fyp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.example.fyp.activities.DestinationGuideActivity;
import com.example.fyp.activities.EcoImpactReportActivity;
import com.example.fyp.activities.EcoTrackActivity;
import com.example.fyp.activities.InteractiveMapActivity;
import com.example.fyp.activities.SustainabilityTipsActivity;
import com.example.fyp.activities.VisitorCheckInActivity;
import com.example.fyp.auth.LoginActivity;

public class MainActivity extends AppCompatActivity {

    private TextView welcomeText;
    private Button btnExploreSites, btnTravelTips, btnMap, btnImpactReport,
            btnCheckIn, btnCarbonFootprint, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- Toolbar setup ---
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // --- Link UI elements ---
        welcomeText = findViewById(R.id.txtWelcome);
        btnExploreSites = findViewById(R.id.btnExploreSites);
        btnTravelTips = findViewById(R.id.btnTravelTips);
        btnMap = findViewById(R.id.btnMap);
        btnImpactReport = findViewById(R.id.btnImpactReport);
        btnCheckIn = findViewById(R.id.btnCheckIn);
        btnCarbonFootprint = findViewById(R.id.btnCarbonFootprint);
        btnLogout = findViewById(R.id.btnLogout);

        // --- Get username dynamically (fallback: "User") ---
        String username = getIntent().getStringExtra("USERNAME_KEY");
        if (username == null || username.trim().isEmpty()) {
            username = "User";
        }
        welcomeText.setText("Welcome, " + username + "!");

        // --- Navigation setup ---
        btnExploreSites.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DestinationGuideActivity.class);
            startActivity(intent);
        });

        btnTravelTips.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SustainabilityTipsActivity.class);
            startActivity(intent);
        });

        btnMap.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, InteractiveMapActivity.class);
            startActivity(intent);
        });

        btnImpactReport.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EcoImpactReportActivity.class);
            startActivity(intent);
        });

        btnCheckIn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, VisitorCheckInActivity.class);
            startActivity(intent);
        });

        btnCarbonFootprint.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EcoTrackActivity.class);
            startActivity(intent);
        });

        // --- Logout handling ---
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // close MainActivity so user can’t go back with back button
        });
    }
}
