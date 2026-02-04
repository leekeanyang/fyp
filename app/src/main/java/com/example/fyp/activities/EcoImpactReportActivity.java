package com.example.fyp.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.fyp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class EcoImpactReportActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int LOCATION_PERMISSION_REQUEST = 2001;

    Spinner spinnerIssueType;
    EditText editDescription;
    EditText editLocation;
    Button btnUploadPhoto, btnSubmitReport, btnUseLocation;
    ImageView imgPreview;
    TextView photoStatus;

    Uri selectedImageUri; // holds uploaded photo

    ProgressDialog progressDialog; // loading dialog
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eco_impact_report);

        spinnerIssueType = findViewById(R.id.spinner_issue_type);
        editDescription = findViewById(R.id.edit_description);
        editLocation = findViewById(R.id.edit_location);
        btnUploadPhoto = findViewById(R.id.btn_upload_photo);
        btnUseLocation = findViewById(R.id.btn_use_location);
        btnSubmitReport = findViewById(R.id.btn_submit_report);
        imgPreview = findViewById(R.id.img_preview);
        photoStatus = findViewById(R.id.tv_photo_status);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

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

        btnUseLocation.setOnClickListener(v -> requestCurrentLocation());

        // Submit button
        btnSubmitReport.setOnClickListener(v -> {
            String issueType = spinnerIssueType.getSelectedItem().toString();
            String description = editDescription.getText().toString();
            String location = editLocation.getText().toString();

            if (description.trim().isEmpty()) {
                editDescription.setError("Please describe the issue");
                editDescription.requestFocus();
                return;
            }

            if (location.trim().isEmpty()) {
                editLocation.setError("Please add a location");
                editLocation.requestFocus();
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
                intent.putExtra("location", location);
                intent.putExtra("photoAttached", selectedImageUri != null);
                startActivity(intent);

                // Reset form
                editDescription.setText("");
                editLocation.setText("");
                spinnerIssueType.setSelection(0);
                imgPreview.setVisibility(ImageView.GONE);
                photoStatus.setText("No photo selected");
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
            photoStatus.setText("Photo selected ✅");
        }
    }

    private void requestCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                String formatted = String.format("Lat %.5f, Lng %.5f",
                        location.getLatitude(), location.getLongitude());
                editLocation.setText(formatted);
            } else {
                Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestCurrentLocation();
        } else if (requestCode == LOCATION_PERMISSION_REQUEST) {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}
