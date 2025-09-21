package com.example.fyp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fyp.R;
import com.google.firebase.database.*;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import org.jspecify.annotations.NonNull;

public class CheckInActivity extends AppCompatActivity {

    TextView txtSiteName, txtVisitorCount, txtEcoCapacity;
    Button btnScanQR, btnManualCheckIn;

    DatabaseReference siteRef;     // SiteCheckins/{siteId}
    String siteId = "royal_belum"; // example — pass via Intent in real app
    int currentCount = 0;

    // launcher for ZXing
    final androidx.activity.result.ActivityResultLauncher<ScanOptions> barcodeLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if(result.getContents() != null) {
                    // Typically QR contains the siteId or other data
                    String scanned = result.getContents();
                    // If the QR contains siteId validate it
                    if(scanned.equals(siteId)) {
                        performCheckIn();
                    } else {
                        Toast.makeText(this, "Invalid QR for this site.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkin);

        txtSiteName = findViewById(R.id.txtSiteName);
        txtVisitorCount = findViewById(R.id.txtVisitorCount);
        txtEcoCapacity = findViewById(R.id.txtEcoCapacity);
        btnScanQR = findViewById(R.id.btnScanQR);
        btnManualCheckIn = findViewById(R.id.btnManualCheckIn);

        txtSiteName.setText("Site: Royal Belum");

        siteRef = FirebaseDatabase.getInstance().getReference("SiteCheckins").child(siteId);

        // Listen for real-time count updates
        siteRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Long count = snapshot.child("count").getValue(Long.class);
                currentCount = (count == null) ? 0 : count.intValue();
                txtVisitorCount.setText("Visitors: " + currentCount);

                // Determine capacity indicator
                // For demo, assume maximum comfortable capacity is 100
                int maxCapacity = 100;
                double ratio = (double) currentCount / maxCapacity;

                String capacityText;
                if (ratio < 0.5) capacityText = "Low";
                else if (ratio < 0.85) capacityText = "Moderate";
                else capacityText = "High";

                txtEcoCapacity.setText("Eco Capacity: " + capacityText);
            }
            @Override public void onCancelled(DatabaseError error) {}
        });

        btnScanQR.setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setPrompt("Scan site QR to check in");
            options.setBeepEnabled(true);
            options.setOrientationLocked(false);
            options.setCaptureActivity(com.journeyapps.barcodescanner.CaptureActivity.class);
            barcodeLauncher.launch(options);
        });

        btnManualCheckIn.setOnClickListener(v -> performCheckIn());
    }

    private void performCheckIn() {
        // Use a transaction to safely increment count
        siteRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.@NonNull Result doTransaction(@NonNull MutableData currentData) {
                Integer cnt = currentData.child("count").getValue(Integer.class);
                if (cnt == null) cnt = 0;
                currentData.child("count").setValue(cnt + 1);
                currentData.child("lastUpdate").setValue(System.currentTimeMillis());
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (committed) {
                    Toast.makeText(CheckInActivity.this, "Checked in — thanks for visiting!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CheckInActivity.this, "Check-in failed. Try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
