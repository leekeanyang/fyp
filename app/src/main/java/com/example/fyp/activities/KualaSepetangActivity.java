package com.example.fyp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fyp.R;

public class KualaSepetangActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kuala_sepetang);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Kuala Sepetang");
        }

        // Buttons
        Button viewOnMapBtn = findViewById(R.id.viewOnMapSepetangBtn);
        Button learnMoreBtn = findViewById(R.id.learnMoreSepetangBtn);
        Button viewGalleryBtn = findViewById(R.id.viewGallerySepetangBtn);

        viewOnMapBtn.setOnClickListener(v -> {
            String mapUrl = "https://www.google.com/maps?q=Kuala+Sepetang";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mapUrl));
            intent.setPackage("com.google.android.apps.maps");
            startActivity(intent);
        });

        learnMoreBtn.setOnClickListener(v -> {
            String infoUrl = "https://visitperak.com.my/kuala-sepetang-port-weld/";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(infoUrl));
            startActivity(intent);
        });

        viewGalleryBtn.setOnClickListener(v -> {
            Intent intent = new Intent(KualaSepetangActivity.this, GalleryActivity.class);
            intent.putExtra(GalleryActivity.EXTRA_SITE, GalleryActivity.SITE_KUALA_SEPETANG);
            startActivity(intent);
        });

        // Collapsible Sections
        setupCollapsible(R.id.activitiesTitle, R.id.activitiesContent);
        setupCollapsible(R.id.foodTitle, R.id.foodContent);
        setupCollapsible(R.id.ecoTitle, R.id.ecoContent);
        setupCollapsible(R.id.sensitiveTitle, R.id.sensitiveContent);
        setupCollapsible(R.id.guidelinesTitle, R.id.guidelinesContent);
        setupCollapsible(R.id.facilitiesTitle, R.id.facilitiesContent);
    }

    private void setupCollapsible(int titleId, int contentId) {
        TextView title = findViewById(titleId);
        TextView content = findViewById(contentId);

        title.setOnClickListener(v -> {
            if (content.getVisibility() == TextView.GONE) {
                fadeIn(content);
                content.setVisibility(TextView.VISIBLE);
            } else {
                content.setVisibility(TextView.GONE);
            }
        });
    }

    private void fadeIn(TextView content) {
        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(300);
        content.startAnimation(anim);
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
