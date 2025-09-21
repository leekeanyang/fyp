package com.example.fyp.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fyp.R;

public class EcoImpactReportActivity extends AppCompatActivity {

    Spinner spinnerIssueType;
    EditText editDescription;
    Button btnUploadPhoto, btnSubmitReport;
    ImageView imgPreview;

    Uri selectedImageUri; // holds uploaded photo
    private static final int PICK_IMAGE_REQUEST = 1;

    ProgressDialog progressDialog; // loading dialog

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eco_impact_report);

        spinnerIssueType = findViewById(R.id.spinner_issue_type);
        editDescription = findViewById(R.id.edit_description);
        btnUploadPhoto = findViewById(R.id.btn_upload_photo);
        btnSubmitReport = findViewById(R.id.btn_submit_report);
        imgPreview = findViewById(R.id.img_preview);

        // Dropdown values with emojis
        String[] issues = {"🌍 Littering", "🦉 Wildlife Disturbance", "🥾 Trail Damage", "❓ Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, issues);
        spinnerIssueType.setAdapter(adapter);

        // Upload photo
        btnUploadPhoto.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        });

        // Submit button
        btnSubmitReport.setOnClickListener(v -> {
            String issueType = spinnerIssueType.getSelectedItem().toString();
            String description = editDescription.getText().toString();

            if (description.trim().isEmpty()) {
                editDescription.setError("Please describe the issue");
                editDescription.requestFocus();
                return;
            }

            // Show loading dialog
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("🌿 Submitting your report...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            // Delay 2 seconds then proceed
            new Handler().postDelayed(() -> {
                progressDialog.dismiss();
                Toast.makeText(this, "Report Submitted ✅", Toast.LENGTH_SHORT).show();

                // Navigate to ReportStatusActivity
                Intent intent = new Intent(EcoImpactReportActivity.this, ReportStatusActivity.class);
                intent.putExtra("issueType", issueType);
                intent.putExtra("description", description);
                intent.putExtra("photoAttached", selectedImageUri != null);
                startActivity(intent);

                // Reset form
                editDescription.setText("");
                spinnerIssueType.setSelection(0);
                imgPreview.setVisibility(ImageView.GONE);
                selectedImageUri = null;
            }, 2000);
        });
    }

    // Handle photo selection
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            imgPreview.setImageURI(selectedImageUri);
            imgPreview.setVisibility(ImageView.VISIBLE);
        }
    }
}
