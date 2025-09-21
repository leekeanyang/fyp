package com.example.fyp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fyp.R;
import com.example.fyp.activities.DestinationGuideActivity;
import com.example.fyp.activities.EcoImpactReportActivity;
import com.example.fyp.activities.EcoTrackActivity;
import com.example.fyp.activities.InteractiveMapActivity;
import com.example.fyp.activities.SustainabilityTipsActivity;
import com.example.fyp.activities.VisitorCheckInActivity;

public class DashboardActivity extends AppCompatActivity {

    Button btnDestinationGuide, btnSustainabilityTips, btnInteractiveMap,
            btnEcoImpactReport, btnVisitorCheckIn, btnEcoTrack, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Bind buttons
        btnLogout = findViewById(R.id.btn_login);

        // Set listeners
        btnDestinationGuide.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, DestinationGuideActivity.class)));

        btnSustainabilityTips.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, SustainabilityTipsActivity.class)));

        btnInteractiveMap.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, InteractiveMapActivity.class)));

        btnEcoImpactReport.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, EcoImpactReportActivity.class)));

        btnVisitorCheckIn.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, VisitorCheckInActivity.class)));

        btnEcoTrack.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, EcoTrackActivity.class)));

        btnLogout.setOnClickListener(v -> {
            // TODO: Clear user session (FirebaseAuth.signOut if using Firebase)
            finish();
        });
    }
}
