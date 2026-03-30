package com.example.fyp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fyp.MainActivity;
import com.example.fyp.R;
import com.example.fyp.models.Report;
import com.example.fyp.repositories.EcoRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReportStatusActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView statusText;
    private EcoRepository ecoRepository;
    private FirebaseAuth firebaseAuth;
    private ValueEventListener reportStatusListener;
    private String activeReportId;
    private String lastSeenStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_status);

        statusText = findViewById(R.id.text_status);
        progressBar = findViewById(R.id.progress_bar);
        Button btnBackHome = findViewById(R.id.btn_back_home);
        Button btnReportAgain = findViewById(R.id.btn_report_again);

        ecoRepository = new EcoRepository();
        firebaseAuth = FirebaseAuth.getInstance();

        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        btnReportAgain.setOnClickListener(v -> {
            startActivity(new Intent(this, EcoImpactReportActivity.class));
            finish();
        });

        String reportId = getIntent().getStringExtra("reportId");
        if (reportId != null && !reportId.trim().isEmpty()) {
            attachReportListener(reportId);
        } else {
            loadLatestReportForCurrentUser();
        }
    }

    private void loadLatestReportForCurrentUser() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            statusText.setText("Please sign in to view your report status.");
            return;
        }

        ecoRepository.loadLatestReportForUser(currentUser.getUid(), new EcoRepository.ReportListener() {
            @Override
            public void onLoaded(Report report) {
                attachReportListener(report.getId());
            }

            @Override
            public void onError(String message) {
                statusText.setText(message);
            }
        });
    }

    private void attachReportListener(String reportId) {
        activeReportId = reportId;
        reportStatusListener = ecoRepository.getReportReference(reportId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Report report = snapshot.getValue(Report.class);
                if (report == null) {
                    statusText.setText("This report could not be found.");
                    return;
                }

                if (report.getId() == null || report.getId().isEmpty()) {
                    report.setId(snapshot.getKey());
                }

                updateStatusView(report);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                statusText.setText("Failed to load report: " + error.getMessage());
            }
        });
    }

    private void updateStatusView(Report report) {
        progressBar.setProgress(report.getStatusStep());

        String formattedCreated = formatTime(report.getTimestamp());
        String formattedUpdated = formatTime(report.getUpdatedAt());
        String adminAction = report.getAdminAction() == null || report.getAdminAction().trim().isEmpty()
                ? "No admin action recorded yet."
                : report.getAdminAction();
        String adminName = report.getAdminName() == null || report.getAdminName().trim().isEmpty()
                ? "Ranger team"
                : report.getAdminName();

        statusText.setText(
                "Report ID: " + report.getId() + "\n\n"
                        + "Site: " + safeValue(report.getSite()) + "\n"
                        + "Issue: " + safeValue(report.getIssueType()) + "\n"
                        + "Location: " + safeValue(report.getLocation()) + "\n"
                        + "Description: " + safeValue(report.getDescription()) + "\n"
                        + "Submitted: " + formattedCreated + "\n"
                        + "Status: " + safeValue(report.getStatus()) + "\n"
                        + "Latest admin update: " + adminAction + "\n"
                        + "Updated by: " + adminName + "\n"
                        + "Last updated: " + formattedUpdated
        );

        if (lastSeenStatus != null && !lastSeenStatus.equalsIgnoreCase(report.getStatus())) {
            Toast.makeText(this, "Report status updated to " + report.getStatus(), Toast.LENGTH_LONG).show();
        }
        lastSeenStatus = report.getStatus();
    }

    private String safeValue(String value) {
        return value == null || value.trim().isEmpty() ? "Not provided" : value;
    }

    private String formatTime(long timestamp) {
        if (timestamp <= 0L) {
            return "Not available";
        }
        return new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(new Date(timestamp));
    }

    @Override
    protected void onDestroy() {
        if (activeReportId != null && reportStatusListener != null) {
            ecoRepository.getReportReference(activeReportId).removeEventListener(reportStatusListener);
        }
        super.onDestroy();
    }
}
