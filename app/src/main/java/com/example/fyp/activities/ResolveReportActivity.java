package com.example.fyp.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fyp.R;
import com.example.fyp.models.Report;
import com.example.fyp.repositories.EcoRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ResolveReportActivity extends AppCompatActivity {

    private TextView tvReportId;
    private TextView tvIssueType;
    private TextView tvLocation;
    private TextView tvDescription;
    private TextView tvCurrentStatus;
    private ImageView ivReportPhoto;
    private Spinner spinnerStatus;
    private EditText etResolution;
    private Button btnResolve;

    private EcoRepository ecoRepository;
    private FirebaseAuth firebaseAuth;
    private String reportId;
    private String currentStatus = "Submitted";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resolve_report);

        tvReportId = findViewById(R.id.tv_report_id);
        tvIssueType = findViewById(R.id.tv_issue_type);
        tvLocation = findViewById(R.id.tv_location);
        tvDescription = findViewById(R.id.tv_description);
        tvCurrentStatus = findViewById(R.id.tv_current_status);
        ivReportPhoto = findViewById(R.id.iv_report_photo);
        spinnerStatus = findViewById(R.id.spinner_status);
        etResolution = findViewById(R.id.et_resolution);
        btnResolve = findViewById(R.id.btn_resolve);

        ecoRepository = new EcoRepository();
        firebaseAuth = FirebaseAuth.getInstance();

        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.report_statuses,
                android.R.layout.simple_spinner_dropdown_item
        );
        spinnerStatus.setAdapter(statusAdapter);

        reportId = getIntent().getStringExtra("reportId");
        if (reportId == null || reportId.trim().isEmpty()) {
            Toast.makeText(this, "No report selected.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadReport();
        btnResolve.setOnClickListener(v -> saveAdminUpdate());
    }

    private void loadReport() {
        ecoRepository.getReportReference(reportId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Report report = snapshot.getValue(Report.class);
                if (report == null) {
                    Toast.makeText(ResolveReportActivity.this, "Unable to load this report.", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                if (report.getId() == null || report.getId().isEmpty()) {
                    report.setId(snapshot.getKey());
                }

                bindReport(report);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ResolveReportActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void bindReport(Report report) {
        currentStatus = report.getStatus();
        tvReportId.setText("Report ID: " + report.getId());
        tvIssueType.setText("Issue Type: " + report.getIssueType());
        tvLocation.setText("Location: " + report.getLocation());
        tvDescription.setText("Description: " + report.getDescription());
        tvCurrentStatus.setText("Current status: " + report.getStatus());
        etResolution.setText(report.getAdminAction() != null ? report.getAdminAction() : "");

        setSpinnerSelection(report.getStatus());

        if (report.getPhotoUrl() != null && !report.getPhotoUrl().trim().isEmpty()) {
            Picasso.get().load(report.getPhotoUrl()).placeholder(R.drawable.placeholder_image).into(ivReportPhoto);
        } else {
            ivReportPhoto.setImageResource(R.drawable.placeholder_image);
        }
    }

    private void setSpinnerSelection(String status) {
        if (status == null) {
            return;
        }
        for (int i = 0; i < spinnerStatus.getCount(); i++) {
            String item = String.valueOf(spinnerStatus.getItemAtPosition(i));
            if (item.equalsIgnoreCase(status.trim())) {
                spinnerStatus.setSelection(i);
                return;
            }
        }
    }

    private void saveAdminUpdate() {
        String selectedStatus = String.valueOf(spinnerStatus.getSelectedItem());
        String adminAction = etResolution.getText().toString().trim();
        if (adminAction.isEmpty()) {
            etResolution.setError("Please describe the action taken.");
            etResolution.requestFocus();
            return;
        }

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        String adminName = currentUser != null && currentUser.getEmail() != null
                ? currentUser.getEmail()
                : "Admin Team";

        ecoRepository.updateReportStatus(reportId, selectedStatus, adminAction, adminName, new EcoRepository.CompletionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(ResolveReportActivity.this, "Admin update saved.", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ResolveReportActivity.this, "Failed to update report: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
