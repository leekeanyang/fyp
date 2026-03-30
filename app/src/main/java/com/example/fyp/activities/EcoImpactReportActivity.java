package com.example.fyp.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.fyp.R;
import com.example.fyp.auth.LoginActivity;
import com.example.fyp.models.Report;
import com.example.fyp.repositories.EcoRepository;
import com.example.fyp.repositories.SiteCatalog;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.List;
import java.util.Locale;

public class EcoImpactReportActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1001;
    private static final int LOCATION_PERMISSION_REQUEST = 1002;
    private static final String TRIP_PREFS = "TripPlannerPrefs";
    private static final String KEY_SELECTED_SITE = "selected_site_name";

    private Spinner spinnerSite;
    private Spinner spinnerIssueType;
    private EditText editDescription;
    private EditText editLocation;
    private Button btnUploadPhoto;
    private Button btnSubmitReport;
    private Button btnUseLocation;
    private Button btnViewStatus;
    private ImageView imgPreview;
    private TextView tvPhotoStatus;
    private View loadingOverlay;
    private TextView tvLoadingMessage;

    private Uri selectedImageUri;
    private Location lastKnownLocation;
    private FusedLocationProviderClient fusedLocationClient;
    private EcoRepository ecoRepository;
    private FirebaseAuth firebaseAuth;
    private StorageReference reportPhotoReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eco_impact_report);

        spinnerSite = findViewById(R.id.spinner_site);
        spinnerIssueType = findViewById(R.id.spinner_issue_type);
        editDescription = findViewById(R.id.edit_description);
        editLocation = findViewById(R.id.edit_location);
        btnUploadPhoto = findViewById(R.id.btn_upload_photo);
        btnSubmitReport = findViewById(R.id.btn_submit_report);
        btnUseLocation = findViewById(R.id.btn_use_location);
        btnViewStatus = findViewById(R.id.btn_view_status);
        imgPreview = findViewById(R.id.img_preview);
        tvPhotoStatus = findViewById(R.id.tv_photo_status);
        loadingOverlay = findViewById(R.id.loading_overlay);
        tvLoadingMessage = findViewById(R.id.tv_loading_message);

        ecoRepository = new EcoRepository();
        ecoRepository.ensureSiteSeeds();
        firebaseAuth = FirebaseAuth.getInstance();
        reportPhotoReference = FirebaseStorage.getInstance().getReference("report_photos");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setupSiteSpinner();
        setupIssueSpinner();
        prefillInputsFromIntent();
        requestCurrentLocation();

        btnUploadPhoto.setOnClickListener(v -> openImagePicker());
        btnUseLocation.setOnClickListener(v -> requestCurrentLocation());
        btnViewStatus.setOnClickListener(v -> openLatestReport());
        btnSubmitReport.setOnClickListener(v -> submitReport());
    }

    private void setupSiteSpinner() {
        List<String> siteNames = SiteCatalog.getSiteNames(false);
        ArrayAdapter<String> siteAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, siteNames);
        spinnerSite.setAdapter(siteAdapter);
    }

    private void setupIssueSpinner() {
        ArrayAdapter<CharSequence> issueAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.issue_types,
                android.R.layout.simple_spinner_dropdown_item
        );
        spinnerIssueType.setAdapter(issueAdapter);
    }

    private void prefillInputsFromIntent() {
        String siteName = getIntent().getStringExtra("siteName");
        if (siteName != null && !siteName.trim().isEmpty()) {
            setSpinnerSelection(spinnerSite, siteName);
            rememberSelectedSite(siteName);
        } else {
            SharedPreferences tripPrefs = getSharedPreferences(TRIP_PREFS, MODE_PRIVATE);
            String rememberedSite = tripPrefs.getString(KEY_SELECTED_SITE, null);
            if (rememberedSite != null) {
                setSpinnerSelection(spinnerSite, rememberedSite);
            }
        }

        if (getIntent().hasExtra("lat") && getIntent().hasExtra("lng")) {
            double latitude = getIntent().getDoubleExtra("lat", 0.0);
            double longitude = getIntent().getDoubleExtra("lng", 0.0);
            editLocation.setText(EcoRepository.buildLocationLabel(latitude, longitude));
        }
    }

    private void setSpinnerSelection(Spinner spinner, String targetValue) {
        if (targetValue == null) {
            return;
        }
        for (int i = 0; i < spinner.getCount(); i++) {
            String value = String.valueOf(spinner.getItemAtPosition(i));
            if (value.equalsIgnoreCase(targetValue.trim())) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private void rememberSelectedSite(String siteName) {
        getSharedPreferences(TRIP_PREFS, MODE_PRIVATE)
                .edit()
                .putString(KEY_SELECTED_SITE, siteName)
                .apply();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select report photo"), PICK_IMAGE_REQUEST);
    }

    private void requestCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                lastKnownLocation = location;
                editLocation.setText(EcoRepository.buildLocationLabel(location.getLatitude(), location.getLongitude()));
            }
        });
    }

    private void submitReport() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please sign in before submitting a report.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        String siteName = String.valueOf(spinnerSite.getSelectedItem());
        String issueType = String.valueOf(spinnerIssueType.getSelectedItem());
        String description = editDescription.getText().toString().trim();
        String locationLabel = editLocation.getText().toString().trim();

        if (description.isEmpty()) {
            editDescription.setError("Please describe the issue.");
            editDescription.requestFocus();
            return;
        }

        String siteId = SiteCatalog.getSiteId(siteName);
        SiteCatalog.SiteMeta meta = SiteCatalog.getSiteMetaById(siteId);
        if (meta == null) {
            Toast.makeText(this, "Please select a valid site.", Toast.LENGTH_SHORT).show();
            return;
        }

        double latitude = lastKnownLocation != null ? lastKnownLocation.getLatitude() : meta.getLatitude();
        double longitude = lastKnownLocation != null ? lastKnownLocation.getLongitude() : meta.getLongitude();
        if (locationLabel.isEmpty()) {
            locationLabel = EcoRepository.buildLocationLabel(latitude, longitude);
            editLocation.setText(locationLabel);
        }

        rememberSelectedSite(siteName);
        showLoading("Submitting report...");

        if (selectedImageUri != null) {
            uploadPhotoAndSaveReport(currentUser, siteId, siteName, issueType, description, locationLabel, latitude, longitude);
        } else {
            saveReport(currentUser, siteId, siteName, issueType, description, locationLabel, latitude, longitude, "");
        }
    }

    private void uploadPhotoAndSaveReport(FirebaseUser user,
                                          String siteId,
                                          String siteName,
                                          String issueType,
                                          String description,
                                          String locationLabel,
                                          double latitude,
                                          double longitude) {
        StorageReference imageReference = reportPhotoReference
                .child(siteId)
                .child(user.getUid() + "_" + System.currentTimeMillis() + ".jpg");

        UploadTask uploadTask = imageReference.putFile(selectedImageUri);
        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful() && task.getException() != null) {
                throw task.getException();
            }
            return imageReference.getDownloadUrl();
        }).addOnSuccessListener(uri -> saveReport(
                user,
                siteId,
                siteName,
                issueType,
                description,
                locationLabel,
                latitude,
                longitude,
                uri.toString()
        )).addOnFailureListener(error -> {
            hideLoading();
            Toast.makeText(this, "Photo upload failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void saveReport(FirebaseUser user,
                            String siteId,
                            String siteName,
                            String issueType,
                            String description,
                            String locationLabel,
                            double latitude,
                            double longitude,
                            String photoUrl) {
        long now = System.currentTimeMillis();
        String reportId = ecoRepository.createReportId();
        Report report = new Report(
                reportId,
                siteId,
                siteName,
                issueType,
                description,
                photoUrl,
                "Submitted",
                locationLabel,
                latitude,
                longitude,
                user.getUid(),
                user.getEmail() != null ? user.getEmail() : "",
                "Awaiting ranger review.",
                "",
                now,
                now
        );

        ecoRepository.submitReport(report, new EcoRepository.CompletionListener() {
            @Override
            public void onSuccess() {
                hideLoading();
                Toast.makeText(EcoImpactReportActivity.this, "Report submitted successfully.", Toast.LENGTH_SHORT).show();
                openReportStatus(reportId);
                resetForm();
            }

            @Override
            public void onError(String message) {
                hideLoading();
                Toast.makeText(EcoImpactReportActivity.this, "Failed to submit report: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void openReportStatus(String reportId) {
        Intent intent = new Intent(this, ReportStatusActivity.class);
        intent.putExtra("reportId", reportId);
        startActivity(intent);
    }

    private void openLatestReport() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please sign in to view your report status.", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading("Loading latest report...");
        ecoRepository.loadLatestReportForUser(currentUser.getUid(), new EcoRepository.ReportListener() {
            @Override
            public void onLoaded(Report report) {
                hideLoading();
                openReportStatus(report.getId());
            }

            @Override
            public void onError(String message) {
                hideLoading();
                Toast.makeText(EcoImpactReportActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetForm() {
        editDescription.setText("");
        tvPhotoStatus.setText("No photo selected");
        imgPreview.setVisibility(View.GONE);
        selectedImageUri = null;
    }

    private void showLoading(String message) {
        tvLoadingMessage.setText(message);
        loadingOverlay.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        loadingOverlay.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                imgPreview.setImageURI(selectedImageUri);
                imgPreview.setVisibility(View.VISIBLE);
                tvPhotoStatus.setText("Photo selected: " + selectedImageUri.getLastPathSegment());
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestCurrentLocation();
        } else if (requestCode == LOCATION_PERMISSION_REQUEST) {
            Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show();
        }
    }
}
