package com.example.fyp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fyp.MainActivity;
import com.example.fyp.R;

public class ReportStatusActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private TextView statusText;

    private int currentStatus = 1; // 1 = Pending, 2 = In Review, 3 = Resolved
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_status);

        statusText = findViewById(R.id.text_status);
        progressBar = findViewById(R.id.progress_bar);
        Button btnBackHome = findViewById(R.id.btn_back_home);
        Button btnReportAgain = findViewById(R.id.btn_report_again);

        // Get report details
        String issueType = getIntent().getStringExtra("issueType");
        String description = getIntent().getStringExtra("description");
        String location = getIntent().getStringExtra("location");
        if (location == null || location.trim().isEmpty()) {
            location = "Not provided";
        }
        boolean photoAttached = getIntent().getBooleanExtra("photoAttached", false);

        // Display initial report details
        statusText.setText("✅ Report Submitted!\n\n"
                + "📌 Issue: " + issueType + "\n"
                + "📍 Location: " + location + "\n"
                + "📝 Description: " + description + "\n"
                + (photoAttached ? "📷 Photo: Attached\n" : "📷 Photo: Not attached\n")
                + "\n📊 Status: Pending");

        progressBar.setProgress(currentStatus);

        // Simulate status updates every 5 seconds
        handler.postDelayed(updateStatusRunnable, 5000);

        // Buttons
        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        btnReportAgain.setOnClickListener(v -> {
            Intent intent = new Intent(this, EcoImpactReportActivity.class);
            startActivity(intent);
            finish();
        });
    }

    // Runnable to simulate progress tracking
    private Runnable updateStatusRunnable = new Runnable() {
        @Override
        public void run() {
            if (currentStatus < 3) {
                currentStatus++;
                progressBar.setProgress(currentStatus);

                if (currentStatus == 2) {
                    statusText.append("\n🔎 Status: In Review...");
                } else if (currentStatus == 3) {
                    statusText.append("\n✅ Status: Resolved!");
                }

                handler.postDelayed(this, 5000); // Update again after 5s
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateStatusRunnable); // stop simulation
    }
}
