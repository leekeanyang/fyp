package com.example.fyp.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.example.fyp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth; // optional
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class ReportActivity extends AppCompatActivity {

    private static final int REQ_PICK_IMAGE = 1001;
    private static final int REQ_LOCATION = 1002;

    Spinner spinnerIssueType;
    EditText editTextDescription;
    TextView textViewLocation;
    Button buttonUploadPhoto, buttonCaptureLocation, buttonSubmitReport;
    ImageView imagePreview;

    Uri selectedImageUri;
    String currentLocationText = "Not captured";
    double latitude = 0, longitude = 0;

    FusedLocationProviderClient fusedLocationClient;
    ProgressDialog progressDialog;

    DatabaseReference reportsRef;
    StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        spinnerIssueType = findViewById(R.id.spinnerIssueType);
        editTextDescription = findViewById(R.id.editTextDescription);
        textViewLocation = findViewById(R.id.textViewLocation);
        buttonUploadPhoto = findViewById(R.id.buttonUploadPhoto);
        imagePreview = findViewById(R.id.imagePreview);
        buttonCaptureLocation = findViewById(R.id.buttonCaptureLocation);
        buttonSubmitReport = findViewById(R.id.buttonSubmitReport);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        // Firebase refs
        reportsRef = FirebaseDatabase.getInstance().getReference("EcoReports");
        storageRef = FirebaseStorage.getInstance().getReference("report_photos");

        // spinner
        String[] issueTypes = {"Litter", "Wildlife Disturbance", "Trail Damage", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, issueTypes);
        spinnerIssueType.setAdapter(adapter);

        buttonUploadPhoto.setOnClickListener(v -> pickImage());
        buttonCaptureLocation.setOnClickListener(v -> captureLocation());
        buttonSubmitReport.setOnClickListener(v -> submitReport());
    }

    private void pickImage() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, REQ_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            imagePreview.setVisibility(ImageView.VISIBLE);
            Glide.with(this).load(selectedImageUri).into(imagePreview);
        }
    }

    private void captureLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOCATION);
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                currentLocationText = "Lat: " + latitude + ", Lng: " + longitude;
                textViewLocation.setText("Location: " + currentLocationText);
            } else {
                textViewLocation.setText("Location: Not available");
            }
        });
    }

    private void submitReport() {
        String issue = spinnerIssueType.getSelectedItem().toString();
        String desc = editTextDescription.getText().toString().trim();

        if (desc.isEmpty()) {
            Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Please choose a photo", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Uploading report...");
        progressDialog.show();

        // filename
        String fileName = UUID.randomUUID().toString() + ".jpg";
        StorageReference photoRef = storageRef.child(fileName);

        photoRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> photoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String photoUrl = uri.toString();

                    String id = reportsRef.push().getKey();
                    if (id == null) id = UUID.randomUUID().toString();

                    // optional: include userId if using auth
                    String userId = null;
                    if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    }

                    long timestamp = System.currentTimeMillis();
                    String timeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(timestamp));

                    Report report = new Report(
                            id,
                            issue,
                            desc,
                            latitude,
                            longitude,
                            photoUrl,
                            "Submitted",
                            userId,
                            timestamp
                    );

                    reportsRef.child(id).setValue(report)
                            .addOnSuccessListener(aVoid -> {
                                progressDialog.dismiss();
                                Toast.makeText(ReportActivity.this, "Report submitted!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(ReportActivity.this, "Failed to save report: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });

                })).addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(ReportActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // Report model class (inside same file or separate)
    public static class Report {
        public String id, issue, description, photoUrl, status, userId;
        public double latitude, longitude;
        public long timestamp;

        public Report() {}

        public Report(String id, String issue, String description, double latitude, double longitude, String photoUrl, String status, String userId, long timestamp) {
            this.id = id;
            this.issue = issue;
            this.description = description;
            this.latitude = latitude;
            this.longitude = longitude;
            this.photoUrl = photoUrl;
            this.status = status;
            this.userId = userId;
            this.timestamp = timestamp;
        }
    }

    // handle permission reply
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQ_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                captureLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
