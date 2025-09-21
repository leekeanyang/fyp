package com.example.fyp.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;

import com.bumptech.glide.Glide;
import com.example.fyp.R;
import com.example.fyp.models.Tip;

public class TipDetailActivity extends AppCompatActivity {

    private TextView title, description, site, errorMessage;
    private ImageView image;
    private Button shareButton, retryButton;
    private ProgressBar progressBar;

    private Tip tip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tip_detail);

        // Setup toolbar with back button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.tip_detail_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enables back button
        }

        // UI components
        title = findViewById(R.id.detail_title);
        description = findViewById(R.id.detail_description);
        site = findViewById(R.id.detail_site);
        image = findViewById(R.id.detail_image);
        shareButton = findViewById(R.id.share_button);
        retryButton = findViewById(R.id.retry_button);
        progressBar = findViewById(R.id.progress_bar);
        errorMessage = findViewById(R.id.error_message);

        // Get Intent data
        tip = getIntent().getParcelableExtra("tip");

        if (tip != null) {
            populateUI(tip);
        } else {
            showErrorState();
        }

        retryButton.setOnClickListener(v -> {
            if (tip != null) {
                populateUI(tip);
            } else {
                Toast.makeText(this, R.string.error_loading_tip, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private void showErrorState() {
        errorMessage.setVisibility(View.VISIBLE);
        retryButton.setVisibility(View.VISIBLE);

        title.setVisibility(View.GONE);
        description.setVisibility(View.GONE);
        site.setVisibility(View.GONE);
        image.setVisibility(View.GONE);
        shareButton.setVisibility(View.GONE);
    }

    private void populateUI(Tip tip) {
        this.tip = tip;

        title.setVisibility(View.VISIBLE);
        description.setVisibility(View.VISIBLE);
        shareButton.setVisibility(View.VISIBLE);
        errorMessage.setVisibility(View.GONE);
        retryButton.setVisibility(View.GONE);

        title.setText(tip.getTitle());
        title.setContentDescription(tip.getTitle());

        description.setText(tip.getDescription());
        description.setContentDescription(tip.getDescription());

        // Handle image
        if (!TextUtils.isEmpty(tip.getImageUrl())) {
            int resId = getResources().getIdentifier(tip.getImageUrl(), "drawable", getPackageName());
            if (resId != 0) {
                Glide.with(this)
                        .load(resId)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .into(image);
                image.setVisibility(View.VISIBLE);
                image.setContentDescription(getString(R.string.tip_image_description, tip.getTitle()));
                image.setOnClickListener(v -> openFullScreenImage(tip.getImageUrl(), tip.getTitle()));
            } else {
                image.setVisibility(View.GONE);
            }
        } else {
            image.setVisibility(View.GONE);
        }

        // Handle related site
        if (!TextUtils.isEmpty(tip.getSite())) {
            site.setText(tip.getSite());
            site.setVisibility(View.VISIBLE);
            site.setContentDescription(getString(R.string.tip_site_description, tip.getSite()));
        } else {
            site.setVisibility(View.GONE);
        }

        // Share button
        shareButton.setOnClickListener(v -> shareTip(tip));
    }

    private void openFullScreenImage(String imageName, String description) {
        Intent intent = new Intent(this, FullScreenImageActivity.class);
        intent.putExtra("image_url", imageName);
        intent.putExtra("image_description", description + " image");

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this, image, "image_transition");

        startActivity(intent, options.toBundle());
    }

    private void shareTip(Tip tip) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");

        StringBuilder shareText = new StringBuilder();
        shareText.append(tip.getTitle()).append(": ").append(tip.getDescription());
        if (!TextUtils.isEmpty(tip.getSite())) {
            shareText.append(" (Related to: ").append(tip.getSite()).append(")");
        }
        shareText.append("\nLearn more at Perak Ecotourism App!");

        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_tip)));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}