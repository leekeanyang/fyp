package com.example.fyp.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.target.Target;
import com.example.fyp.R;
import com.example.fyp.models.Report;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ReportDetailsActivity extends AppCompatActivity {
    private TextView site, issueType, description, status, location, timestamp;
    private ImageView photo;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private ListenerRegistration listener;
    private Report report;
    private SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_details);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        site = findViewById(R.id.report_site);
        issueType = findViewById(R.id.report_issue_type);
        description = findViewById(R.id.report_description);
        status = findViewById(R.id.report_status);
        location = findViewById(R.id.report_location);
        timestamp = findViewById(R.id.report_timestamp);
        photo = findViewById(R.id.report_photo);
        progressBar = findViewById(R.id.progress_bar);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.report_details_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get report from intent
        report = (Report) getIntent().getSerializableExtra("report");
        if (report == null) {
            showErrorAndFinish("Report data not found");
            return;
        }

        // Bind initial data
        bindReportData(report);

        // Set up map
        setupMap();

        // Set up photo click listener
        photo.setOnClickListener(v -> {
            if (report.getPhotoUrl() != null && !report.getPhotoUrl().isEmpty()) {
                try {
                    Uri uri = Uri.parse(report.getPhotoUrl());
                    if (uri.getScheme() != null && (uri.getScheme().equals("http") || uri.getScheme().equals("https"))) {
                        startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    } else {
                        Toast.makeText(this, "Invalid photo URL", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Unable to open photo", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set up real-time listener
        if (isNetworkAvailable()) {
            setupRealtimeListener();
        } else {
            Toast.makeText(this, "No internet connection. Real-time updates disabled.", Toast.LENGTH_LONG).show();
        }
    }

    private void setupMap() {
        if (report.getLocation() != null && report.getLocation().contains(",")) {
            try {
                String[] coords = report.getLocation().split(",");
                double latitude = Double.parseDouble(coords[0]);
                double longitude = Double.parseDouble(coords[1]);
                LatLng latLng = new LatLng(latitude, longitude);
                mapFragment.getMapAsync(googleMap -> {
                    googleMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(report.getSite())
                            .snippet(report.getIssueType()));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
                    googleMap.getUiSettings().setZoomControlsEnabled(true);
                });
            } catch (NumberFormatException e) {
                mapFragment.getView().setVisibility(View.GONE);
                Toast.makeText(this, "Invalid location coordinates", Toast.LENGTH_SHORT).show();
            }
        } else {
            mapFragment.getView().setVisibility(View.GONE);
        }
    }

    private void bindReportData(Report report) {
        site.setText(report.getSite());
        site.setContentDescription(getString(R.string.report_site_description, report.getSite()));
        issueType.setText(report.getIssueType());
        issueType.setContentDescription(getString(R.string.report_issue_type_description, report.getIssueType()));
        description.setText(report.getDescription());
        description.setContentDescription(getString(R.string.report_description_description, report.getDescription()));
        status.setText(report.getStatus());
        status.setContentDescription(getString(R.string.report_status_description, report.getStatus()));
        timestamp.setText(android.text.format.DateFormat.format("dd/MM/yyyy HH:mm", new java.util.Date(report.getTimestamp())));
        timestamp.setContentDescription(getString(R.string.report_timestamp_description, timestamp.getText()));

        // Handle location with reverse geocoding
        String locationText = report.getLocation();
        if (locationText != null && locationText.contains(",")) {
            try {
                String[] coords = locationText.split(",");
                double latitude = Double.parseDouble(coords[0]);
                double longitude = Double.parseDouble(coords[1]);
                if (isNetworkAvailable()) {
                    Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                        if (!addresses.isEmpty()) {
                            Address address = addresses.get(0);
                            locationText = address.getAddressLine(0) != null ? address.getAddressLine(0) : locationText;
                        }
                    } catch (IOException e) {
                        Toast.makeText(this, "Unable to fetch address", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (NumberFormatException e) {
                // Fallback to raw coordinates
            }
        }
        location.setText(locationText);
        location.setContentDescription(getString(R.string.report_location_description, locationText));

        // Load photo
        if (report.getPhotoUrl() != null && !report.getPhotoUrl().isEmpty()) {
            progressBar.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(report.getPhotoUrl())
                    .thumbnail(0.1f) // Smaller thumbnail for faster loading
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                        public boolean onLoadFailed(Exception e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(ReportDetailsActivity.this, "Failed to load photo", Toast.LENGTH_SHORT).show();
                            return false;
                        }

                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            photo.setVisibility(View.VISIBLE);
                            return false;
                        }
                    })
                    .into(photo);
        } else {
            photo.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        }
    }

    private void setupRealtimeListener() {
        if (report.getId() != null) {
            DocumentReference reportRef = db.collection("reports").document(report.getId());
            listener = reportRef.addSnapshotListener((snapshot, e) -> {
                if (e != null) {
                    Toast.makeText(this, "Error fetching updates: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (snapshot != null && snapshot.exists()) {
                    Report updatedReport = snapshot.toObject(Report.class);
                    if (updatedReport != null) {
                        updatedReport.setId(snapshot.getId());
                        this.report = updatedReport;
                        bindReportData(updatedReport);
                    }
                }
            });
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private void showErrorAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listener != null) {
            listener.remove();
        }
        Glide.with(this).clear(photo);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}