package com.example.fyp.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.example.fyp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class VisitorCheckInActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;
    private static final String PREFS_NAME = "CheckInPrefs";
    private static final String KEY_CHECKINS = "checkin_count";
    private static final String KEY_LAST_CHECKIN = "last_checkin";

    private FusedLocationProviderClient fusedLocationClient;
    private TextView txtStatus;
    private ProgressBar progressCheckin;
    private ImageView ivCheckinBadge;
    private Button btnCheckInQR, btnCheckInGPS, btnCheckInHistory;
    private SharedPreferences prefs;
    private int checkInCount = 0;
    private long lastCheckIn = 0;
    private ActivityResultLauncher<Intent> voiceLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitor_check_in);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        checkInCount = prefs.getInt(KEY_CHECKINS, 0);
        lastCheckIn = prefs.getLong(KEY_LAST_CHECKIN, 0);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize UI
        txtStatus = findViewById(R.id.txtStatus);
        progressCheckin = findViewById(R.id.progress_checkin);
        ivCheckinBadge = findViewById(R.id.iv_checkin_badge);
        btnCheckInQR = findViewById(R.id.btnCheckInQR);
        btnCheckInGPS = findViewById(R.id.btnCheckInGPS);
        btnCheckInHistory = findViewById(R.id.btnCheckInHistory);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // QR Check-In with Voice
        btnCheckInQR.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            startVoiceInputForQR();
        });

        // GPS Check-In
        btnCheckInGPS.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST);
            } else {
                checkInWithGPS();
            }
        });

        // Check-In History
        btnCheckInHistory.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            String history = "Past Check-Ins: " + checkInCount + " times (Last: " + formatLastCheckIn() + ")";
            Toast.makeText(this, history, Toast.LENGTH_LONG).show();
        });

        // Voice Launcher
        voiceLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                ArrayList<String> matches = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (matches != null && !matches.isEmpty()) {
                    new IntentIntegrator(VisitorCheckInActivity.this)
                            .setPrompt("Scan QR: " + matches.get(0))
                            .setOrientationLocked(false)
                            .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                            .initiateScan();
                }
            }
        });
    }

    private void startVoiceInputForQR() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say 'Scan QR' to start...");
        voiceLauncher.launch(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                progressCheckin.setVisibility(View.VISIBLE);
                validateAndCheckIn("QR: " + result.getContents());
            } else {
                Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void checkInWithGPS() {
        progressCheckin.setVisibility(View.VISIBLE);
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                // Mock geofencing for Royal Belum (5.710278, 101.532222)
                Location royalBelum = new Location("");
                royalBelum.setLatitude(5.710278);
                royalBelum.setLongitude(101.532222);
                float distance = location.distanceTo(royalBelum) / 1000; // In km
                if (distance < 10) { // 10km radius
                    validateAndCheckIn("GPS: Lat " + location.getLatitude() + ", Lng " + location.getLongitude());
                } else {
                    txtStatus.setText("Outside check-in area (10km from Royal Belum)");
                    progressCheckin.setVisibility(View.GONE);
                    Toast.makeText(this, "Too far from a check-in site", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show();
                progressCheckin.setVisibility(View.GONE);
            }
        });
    }

    private void validateAndCheckIn(String checkInData) {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        if (currentTime - lastCheckIn > 24 * 60 * 60 * 1000) { // 24-hour cooldown
            checkInCount++;
            lastCheckIn = currentTime;
            prefs.edit().putInt(KEY_CHECKINS, checkInCount).putLong(KEY_LAST_CHECKIN, lastCheckIn).apply();
            txtStatus.setText("Checked in: " + checkInData);
            txtStatus.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
            ivCheckinBadge.setVisibility(View.VISIBLE);
            ivCheckinBadge.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bounce));
            Toast.makeText(this, "Check-in successful! Count: " + checkInCount, Toast.LENGTH_SHORT).show();
        } else {
            txtStatus.setText("Already checked in today");
            progressCheckin.setVisibility(View.GONE);
            Toast.makeText(this, "One check-in per day allowed", Toast.LENGTH_SHORT).show();
        }
        progressCheckin.setVisibility(View.GONE);
    }

    private String formatLastCheckIn() {
        if (lastCheckIn == 0) return "Never";
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(lastCheckIn);
        return String.format("%02d/%02d/%04d %02d:%02d",
                cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR),
                cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkInWithGPS();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}