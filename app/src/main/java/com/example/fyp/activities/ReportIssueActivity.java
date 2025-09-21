package com.example.fyp.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import com.example.fyp.R;
import com.example.fyp.models.Report;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReportIssueActivity extends AppCompatActivity {
    private Spinner siteSelector, issueType;
    private TextInputEditText description;
    private TextInputLayout descriptionLayout;
    private ImageView photoPreview;
    private Button uploadPhoto, submitReport;
    private ProgressBar progressBar;
    private Uri photoUri;
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private List<String> destinations;
    private ActivityResultLauncher<Intent> photoPicker;
    private static final int MAX_PHOTO_SIZE = 5 * 1024 * 1024; // 5MB
    private static final int LOCATION_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_issue);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.report_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize UI components
        siteSelector = findViewById(R.id.site_selector);
        issueType = findViewById(R.id.issue_type);
        descriptionLayout = findViewById(R.id.description_layout);
        description = findViewById(R.id.description);
        photoPreview = findViewById(R.id.photo_preview);
        uploadPhoto = findViewById(R.id.upload_photo);
        submitReport = findViewById(R.id.submit_report);
        progressBar = findViewById(R.id.progress_bar);

        // Set up spinners
        setupSpinners();

        // Set up photo picker
        photoPicker = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                photoUri = result.getData().getData();
                File photoFile = new File(photoUri.getPath());
                if (photoFile.length() > MAX_PHOTO_SIZE) {
                    Toast.makeText(this, R.string.error_photo_too_large, Toast.LENGTH_SHORT).show();
                    return;
                }
                photoPreview.setImageURI(photoUri);
                photoPreview.setVisibility(View.VISIBLE);
            }
        });

        // Photo upload button
        uploadPhoto.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.select_photo_source)
                    .setItems(new String[]{getString(R.string.camera), getString(R.string.gallery)}, (dialog, which) -> {
                        Intent intent;
                        if (which == 0) {
                            // Camera
                            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                                    != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 2);
                                return;
                            }
                            intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        } else {
                            // Gallery
                            intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("image/*");
                        }
                        photoPicker.launch(intent);
                    })
                    .show();
        });

        // Submit button
        submitReport.setOnClickListener(v -> confirmSubmission());
    }

    private void setupSpinners() {
        // Issue type spinner
        ArrayAdapter<CharSequence> issueAdapter = ArrayAdapter.createFromResource(
                this, R.array.issue_types, android.R.layout.simple_spinner_item);
        issueAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        issueType.setAdapter(issueAdapter);

        // Site selector spinner
        destinations = new ArrayList<>();
        destinations.add("Select a site");
        db.collection("destinations").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String siteName = document.getString("name");
                        if (siteName != null && !destinations.contains(siteName)) {
                            destinations.add(siteName);
                        }
                    }
                    ArrayAdapter<String> siteAdapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_item, destinations);
                    siteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    siteSelector.setAdapter(siteAdapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, R.string.error_loading_destinations, Toast.LENGTH_SHORT).show();
                    // Fallback static list
                    destinations.add("Royal Belum State Park");
                    destinations.add("Gua Tempurung");
                    destinations.add("Kuala Sepetang");
                    ArrayAdapter<String> siteAdapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_item, destinations);
                    siteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    siteSelector.setAdapter(siteAdapter);
                });
    }

    private void confirmSubmission() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_submission)
                .setMessage(R.string.confirm_submission_message)
                .setPositiveButton(R.string.submit, (dialog, which) -> submitReport())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void submitReport() {
        // Validate inputs
        if (siteSelector.getSelectedItemPosition() == 0) {
            Toast.makeText(this, R.string.error_select_site, Toast.LENGTH_SHORT).show();
            return;
        }
        String issue = issueType.getSelectedItem().toString();
        String desc = description.getText().toString().trim();
        if (desc.isEmpty()) {
            descriptionLayout.setError(getString(R.string.error_empty_description));
            return;
        }
        descriptionLayout.setError(null);

        // Check location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        submitReport.setEnabled(false);

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            String loc;
            if (location == null) {
                // Prompt user to enable location or manually select
                new AlertDialog.Builder(this)
                        .setTitle(R.string.location_unavailable)
                        .setMessage(R.string.enable_location_message)
                        .setPositiveButton(R.string.settings, (dialog, which) -> {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            progressBar.setVisibility(View.GONE);
                            submitReport.setEnabled(true);
                        })
                        .setNegativeButton(R.string.cancel, (dialog, which) -> {
                            progressBar.setVisibility(View.GONE);
                            submitReport.setEnabled(true);
                        })
                        .show();
                return;
            }
            loc = location.getLatitude() + "," + location.getLongitude();
            String site = siteSelector.getSelectedItem().toString();

            if (photoUri != null) {
                // Upload photo to Firebase Storage
                StorageReference photoRef = storage.getReference().child("reports/" + UUID.randomUUID().toString());
                photoRef.putFile(photoUri)
                        .addOnSuccessListener(taskSnapshot -> photoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            saveReport(loc, issue, desc, uri.toString(), site);
                        }))
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, R.string.error_uploading_photo, Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            submitReport.setEnabled(true);
                        });
            } else {
                saveReport(loc, issue, desc, null, site);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, R.string.error_getting_location, Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            submitReport.setEnabled(true);
        });
    }

    private void saveReport(String location, String issue, String description, String photoUrl, String site) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "anonymous";
        Report report = new Report(location, issue, description, photoUrl, "Submitted", site, userId, System.currentTimeMillis());

        db.collection("reports").add(report)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, R.string.report_submitted, Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, R.string.error_submitting_report, Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    submitReport.setEnabled(true);
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            submitReport();
        } else if (requestCode == 2 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            photoPicker.launch(intent);
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(requestCode == LOCATION_PERMISSION_CODE ? R.string.location_permission_denied : R.string.camera_permission_denied)
                    .setMessage(requestCode == LOCATION_PERMISSION_CODE ? R.string.location_permission_rationale : R.string.camera_permission_rationale)
                    .setPositiveButton(R.string.settings, (dialog, which) -> {
                        startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                .setData(Uri.parse("package:" + getPackageName())));
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}