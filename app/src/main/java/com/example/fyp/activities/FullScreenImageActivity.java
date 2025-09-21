package com.example.fyp.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.fyp.R;

public class FullScreenImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        // Setup toolbar with back button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false); // Hide title for full-screen effect
        }

        ImageView imageView = findViewById(R.id.full_screen_image);
        String imageUrl = getIntent().getStringExtra("image_url");
        String imageDescription = getIntent().getStringExtra("image_description");

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(imageView);
            imageView.setContentDescription(imageDescription != null ? imageDescription : "Full screen image");
        } else {
            imageView.setImageResource(R.drawable.placeholder_image);
            imageView.setContentDescription("No image available");
            Toast.makeText(this, "Image not available", Toast.LENGTH_SHORT).show();
        }

        imageView.setOnClickListener(v -> finish()); // Close on image click
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Close activity on back button press
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}