package com.example.fyp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;

import com.bumptech.glide.Glide;
import com.example.fyp.R;
import com.example.fyp.models.Tip;

import java.util.Locale;

public class TipDetailActivity extends AppCompatActivity {

    private TextView title, description, site, txtCompleted;
    private ImageView image, ivCheck;
    private Button shareButton;
    private Tip tip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tip_detail);

        initViews();
        setupToolbar();

        // Get Parcelable data
        tip = getIntent().getParcelableExtra("tip");

        if (tip != null) {
            populateUI(tip);
        } else {
            showErrorState();
        }
    }

    private void initViews() {
        title = findViewById(R.id.detail_title);
        description = findViewById(R.id.detail_description);
        site = findViewById(R.id.detail_site);
        image = findViewById(R.id.detail_image);
        shareButton = findViewById(R.id.share_button);
        txtCompleted = findViewById(R.id.txt_completed_status);
        ivCheck = findViewById(R.id.iv_check_detail);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Sustainability Tip");
        }
    }

    private void populateUI(Tip tip) {
        title.setText(tip.getTitle());
        description.setText(tip.getDescription());

        // Show site if available
        if (!TextUtils.isEmpty(tip.getSite())) {
            site.setText(String.format("Location: %s", tip.getSite()));
            site.setVisibility(View.VISIBLE);
        } else {
            site.setVisibility(View.GONE);
        }

        // Handle Image loading with Glide
        String imgName = tip.getImageUrl();
        int resId = getResources().getIdentifier(imgName, "drawable", getPackageName());
        Glide.with(this)
                .load(resId != 0 ? resId : R.drawable.ic_eco_tip)
                .placeholder(R.drawable.ic_eco_tip_placeholder)
                .into(image);

        // Completion Status
        if (tip.isCompleted()) {
            if (txtCompleted != null) txtCompleted.setVisibility(View.VISIBLE);
            if (ivCheck != null) ivCheck.setVisibility(View.VISIBLE);
        }

        shareButton.setOnClickListener(v -> shareTip(tip));
        image.setOnClickListener(v -> openFullScreen(tip));
    }

    private void showErrorState() {
        Toast.makeText(this, "Error: Tip data not found", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void shareTip(Tip tip) {
        String shareText = String.format(Locale.getDefault(), 
            "Perak Ecotourism Tip: %s\n\n%s\n\nDownload the app to learn more!", 
            tip.getTitle(), tip.getDescription());
            
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(intent, "Share Tip"));
    }

    private void openFullScreen(Tip tip) {
        // Implement transition if needed, or simple dialog
        Toast.makeText(this, "Opening full screen view...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
