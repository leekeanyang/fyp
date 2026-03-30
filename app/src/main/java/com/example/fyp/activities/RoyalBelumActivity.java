package com.example.fyp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fyp.R;

public class RoyalBelumActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_royal_belum);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Royal Belum");
        }

        // Buttons
        Button viewOnMapBtn = findViewById(R.id.viewOnMapBelumBtn);
        Button learnMoreBtn = findViewById(R.id.learnMoreBelumBtn);
        Button viewGalleryBtn = findViewById(R.id.viewGalleryBelumBtn);

        viewOnMapBtn.setOnClickListener(v -> {
            String mapUrl = "https://www.google.com/maps?q=Royal+Belum+State+Park";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mapUrl));
            intent.setPackage("com.google.android.apps.maps");
            startActivity(intent);
        });

        learnMoreBtn.setOnClickListener(v -> {
            String infoUrl = "https://www.belumrainforestresort.com/";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(infoUrl));
            startActivity(intent);
        });

        viewGalleryBtn.setOnClickListener(v -> {
            Intent intent = new Intent(RoyalBelumActivity.this, GalleryActivity.class);
            intent.putExtra(GalleryActivity.EXTRA_SITE, GalleryActivity.SITE_ROYAL_BELUM);
            startActivity(intent);
        });

        // Collapsible Sections
        setupCollapsible(R.id.activitiesTitle, R.id.activitiesContent);
        setupCollapsible(R.id.wildlifeTitle, R.id.wildlifeContent);
        setupCollapsible(R.id.staysTitle, R.id.staysContent);
        setupCollapsible(R.id.sensitiveTitle, R.id.sensitiveContent);
        setupCollapsible(R.id.guidelinesTitle, R.id.guidelinesContent);
        setupCollapsible(R.id.facilitiesTitle, R.id.facilitiesContent);
    }

    private void setupCollapsible(int titleId, int contentId) {
        TextView title = findViewById(titleId);
        TextView content = findViewById(contentId);

        title.setOnClickListener(v -> {
            if (content.getVisibility() == TextView.GONE) {
                content.setVisibility(TextView.VISIBLE);
            } else {
                content.setVisibility(TextView.GONE);
            }
        });
    }

    // Handle ActionBar back button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
