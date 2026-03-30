package com.example.fyp.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.example.fyp.R;
import com.example.fyp.models.CheckInRecord;
import com.example.fyp.repositories.EcoRepository;
import com.example.fyp.repositories.SiteCatalog;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.qrcode.QRCodeWriter;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class VisitorCheckInActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;
    private static final String PREFS_NAME = "CheckInPrefs";

    private Spinner spinnerSite;
    private TextView txtStatus;
    private TextView txtCapacityLabel;
    private TextView txtCapacityHint;
    private TextView txtCapacityCount;
    private ProgressBar progressCheckin;
    private LinearProgressIndicator capacityIndicator;
    private ImageView ivCheckinBadge;
    private Button btnCheckInQR;
    private Button btnCheckInGPS;
    private Button btnCheckInHistory;
    private Button btnShowSiteQr;

    private FusedLocationProviderClient fusedLocationClient;
    private SharedPreferences prefs;
    private EcoRepository ecoRepository;
    private FirebaseAuth firebaseAuth;
    private final Map<String, Integer> liveSiteCounts = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitor_check_in);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        ecoRepository = new EcoRepository();
        ecoRepository.ensureSiteSeeds();
        firebaseAuth = FirebaseAuth.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        spinnerSite = findViewById(R.id.spinnerSite);
        txtStatus = findViewById(R.id.txtStatus);
        progressCheckin = findViewById(R.id.progress_checkin);
        txtCapacityLabel = findViewById(R.id.txtCapacityLabel);
        txtCapacityHint = findViewById(R.id.txtCapacityHint);
        txtCapacityCount = findViewById(R.id.txtCapacityCount);
        capacityIndicator = findViewById(R.id.progress_capacity);
        ivCheckinBadge = findViewById(R.id.iv_checkin_badge);
        btnCheckInQR = findViewById(R.id.btnCheckInQR);
        btnCheckInGPS = findViewById(R.id.btnCheckInGPS);
        btnCheckInHistory = findViewById(R.id.btnCheckInHistory);
        btnShowSiteQr = findViewById(R.id.btnShowSiteQr);

        setupSiteSpinner();
        btnCheckInQR.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            startQrScanner();
        });
        btnCheckInGPS.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            startGpsCheckIn();
        });
        btnCheckInHistory.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            showHistory();
        });
        btnShowSiteQr.setOnClickListener(v -> showSelectedSiteQr());

        loadSiteCounts();
    }

    private void setupSiteSpinner() {
        List<String> siteNames = SiteCatalog.getSiteNames(false);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, siteNames);
        spinnerSite.setAdapter(adapter);
        spinnerSite.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                updateCapacityUI(getSelectedSiteId());
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    private void loadSiteCounts() {
        ecoRepository.loadSiteCounts(new EcoRepository.SiteCountsListener() {
            @Override
            public void onLoaded(Map<String, Integer> siteCounts) {
                liveSiteCounts.clear();
                liveSiteCounts.putAll(siteCounts);
                updateCapacityUI(getSelectedSiteId());
            }

            @Override
            public void onError(String message) {
                Toast.makeText(VisitorCheckInActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startQrScanner() {
        new IntentIntegrator(this)
                .setPrompt("Scan the entrance QR code")
                .setOrientationLocked(false)
                .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                .initiateScan();
    }

    private void startGpsCheckIn() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            return;
        }

        progressCheckin.setVisibility(View.VISIBLE);
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            progressCheckin.setVisibility(View.GONE);
            if (location == null) {
                Toast.makeText(this, "Unable to detect your current location.", Toast.LENGTH_SHORT).show();
                return;
            }

            SiteCatalog.SiteMeta nearestSite = findNearestSite(location);
            if (nearestSite == null) {
                txtStatus.setText("You are not close enough to a registered site for GPS check-in.");
                return;
            }

            setSpinnerSelection(nearestSite.getName());
            performCheckIn(nearestSite.getId(), "GPS", "");
        });
    }

    private SiteCatalog.SiteMeta findNearestSite(Location location) {
        SiteCatalog.SiteMeta bestMatch = null;
        float bestDistance = Float.MAX_VALUE;

        for (SiteCatalog.SiteMeta meta : SiteCatalog.getSiteMetas()) {
            float[] result = new float[1];
            Location.distanceBetween(
                    location.getLatitude(),
                    location.getLongitude(),
                    meta.getLatitude(),
                    meta.getLongitude(),
                    result
            );
            if (result[0] < bestDistance) {
                bestDistance = result[0];
                bestMatch = meta;
            }
        }

        return bestDistance <= 5000f ? bestMatch : null;
    }

    private void performCheckIn(String siteId, String method, String qrPayload) {
        if (!canCheckInToday(siteId)) {
            txtStatus.setText("You already checked into this site today. Please try again tomorrow.");
            Toast.makeText(this, "Daily check-in limit reached for this site.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        String userId = currentUser != null ? currentUser.getUid() : "guest_user";
        String userEmail = currentUser != null && currentUser.getEmail() != null ? currentUser.getEmail() : "guest";

        progressCheckin.setVisibility(View.VISIBLE);
        ecoRepository.recordCheckIn(siteId, userId, userEmail, method, qrPayload, new EcoRepository.CheckInListener() {
            @Override
            public void onCheckedIn(CheckInRecord record, int currentCount, String capacityLabel) {
                progressCheckin.setVisibility(View.GONE);
                prefs.edit()
                        .putLong(buildLastCheckInKey(siteId), System.currentTimeMillis())
                        .putString("last_site", record.getSiteName())
                        .putString("last_method", method)
                        .apply();

                liveSiteCounts.put(siteId, currentCount);
                txtStatus.setText("Checked in to " + record.getSiteName() + " via " + method + ".");
                txtStatus.startAnimation(AnimationUtils.loadAnimation(VisitorCheckInActivity.this, android.R.anim.fade_in));
                ivCheckinBadge.setVisibility(View.VISIBLE);
                ivCheckinBadge.startAnimation(AnimationUtils.loadAnimation(VisitorCheckInActivity.this, R.anim.bounce));
                updateCapacityUI(siteId);
                Toast.makeText(VisitorCheckInActivity.this, "Check-in recorded.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                progressCheckin.setVisibility(View.GONE);
                Toast.makeText(VisitorCheckInActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean canCheckInToday(String siteId) {
        long lastCheckIn = prefs.getLong(buildLastCheckInKey(siteId), 0L);
        long now = System.currentTimeMillis();
        return now - lastCheckIn >= 24L * 60L * 60L * 1000L;
    }

    private String buildLastCheckInKey(String siteId) {
        return "last_checkin_" + siteId;
    }

    private void showHistory() {
        String siteName = prefs.getString("last_site", "No previous site");
        String method = prefs.getString("last_method", "Unknown");
        long lastTime = 0L;
        for (SiteCatalog.SiteMeta meta : SiteCatalog.getSiteMetas()) {
            lastTime = Math.max(lastTime, prefs.getLong(buildLastCheckInKey(meta.getId()), 0L));
        }

        String formattedTime = lastTime > 0
                ? new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(lastTime)
                : "Never";
        String history = "Latest check-in: " + siteName + "\nMethod: " + method + "\nTime: " + formattedTime;
        Toast.makeText(this, history, Toast.LENGTH_LONG).show();
    }

    private void showSelectedSiteQr() {
        String siteId = getSelectedSiteId();
        SiteCatalog.SiteMeta meta = SiteCatalog.getSiteMetaById(siteId);
        if (meta == null) {
            Toast.makeText(this, "Select a site first.", Toast.LENGTH_SHORT).show();
            return;
        }

        String payload = SiteCatalog.buildQrPayload(siteId);
        ImageView qrImage = new ImageView(this);
        qrImage.setImageBitmap(generateQrBitmap(payload, 700, 700));
        qrImage.setAdjustViewBounds(true);
        qrImage.setPadding(24, 24, 24, 24);

        new AlertDialog.Builder(this)
                .setTitle(meta.getName() + " QR")
                .setView(qrImage)
                .setMessage(payload)
                .setPositiveButton("Close", null)
                .show();
    }

    private Bitmap generateQrBitmap(String payload, int width, int height) {
        try {
            BitMatrix bitMatrix = new QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, width, height);
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bitmap;
        } catch (WriterException exception) {
            throw new IllegalStateException("Unable to generate QR bitmap", exception);
        }
    }

    private void updateCapacityUI(String siteId) {
        SiteCatalog.SiteMeta meta = SiteCatalog.getSiteMetaById(siteId);
        if (meta == null) {
            return;
        }

        int currentCount = liveSiteCounts.containsKey(siteId)
                ? liveSiteCounts.get(siteId)
                : meta.getDefaultVisitorCount();
        int maxCapacity = meta.getMaxCapacity();
        String status = EcoRepository.capacityLabel(currentCount, maxCapacity);

        capacityIndicator.setMax(maxCapacity);
        capacityIndicator.setProgress(currentCount);
        txtCapacityCount.setText(meta.getName() + ": " + currentCount + "/" + maxCapacity + " visitors");
        txtCapacityLabel.setText("Eco-Capacity: " + status);

        if ("Low".equals(status)) {
            txtCapacityLabel.setTextColor(getColor(R.color.green));
            txtCapacityHint.setText("Low crowding. This is a good time to visit.");
        } else if ("Moderate".equals(status)) {
            txtCapacityLabel.setTextColor(getColor(R.color.eco_orange));
            txtCapacityHint.setText("Moderate crowding. Consider quieter trails or earlier entry times.");
        } else {
            txtCapacityLabel.setTextColor(getColor(R.color.eco_red));
            txtCapacityHint.setText("High crowding. Visitors may be redirected to lower-impact alternatives.");
        }
    }

    private String getSelectedSiteId() {
        Object selected = spinnerSite.getSelectedItem();
        return SiteCatalog.getSiteId(String.valueOf(selected));
    }

    private void setSpinnerSelection(String siteName) {
        for (int i = 0; i < spinnerSite.getCount(); i++) {
            String value = String.valueOf(spinnerSite.getItemAtPosition(i));
            if (value.equalsIgnoreCase(siteName)) {
                spinnerSite.setSelection(i);
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                String siteId = SiteCatalog.getSiteIdFromQrPayload(result.getContents());
                if (siteId == null || siteId.trim().isEmpty()) {
                    Toast.makeText(this, "This QR code is not linked to a supported ecotourism site.", Toast.LENGTH_SHORT).show();
                    return;
                }

                SiteCatalog.SiteMeta meta = SiteCatalog.getSiteMetaById(siteId);
                if (meta != null) {
                    setSpinnerSelection(meta.getName());
                }
                performCheckIn(siteId, "QR", result.getContents());
            } else {
                Toast.makeText(this, "Scan cancelled.", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startGpsCheckIn();
        } else if (requestCode == LOCATION_PERMISSION_REQUEST) {
            Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
