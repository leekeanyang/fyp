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

public class GuaTempurungActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gua_tempurung);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Gua Tempurung");
        }

        // Buttons
        Button viewOnMapBtn = findViewById(R.id.viewOnMapTempurungBtn);
        Button learnMoreBtn = findViewById(R.id.learnMoreTempurungBtn);

        viewOnMapBtn.setOnClickListener(v -> {
            String mapUrl = "https://www.google.com/maps?q=Gua+Tempurung+Perak";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mapUrl));
            intent.setPackage("com.google.android.apps.maps");
            startActivity(intent);
        });

        learnMoreBtn.setOnClickListener(v -> {
            String infoUrl = "https://www.tourismperakmalaysia.com/home/gua-tempurung/";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(infoUrl));
            startActivity(intent);
        });

        // Collapsible Sections
        setupCollapsible(R.id.highlightsTitle, R.id.highlightsContent);
        setupCollapsible(R.id.toursTitle, R.id.toursContent);
        setupCollapsible(R.id.tipsTitle, R.id.tipsContent);
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
