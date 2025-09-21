package com.example.fyp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fyp.R;

public class DestinationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination_template);

        ImageView imageView = findViewById(R.id.destinationImage);
        TextView titleView = findViewById(R.id.destinationTitle);
        TextView descView = findViewById(R.id.destinationDesc);
        TextView highlightsView = findViewById(R.id.destinationHighlights);
        Button mapBtn = findViewById(R.id.viewOnMapBtn);
        Button extraBtn = findViewById(R.id.extraActionBtn);

        // Get data passed via Intent
        String title = getIntent().getStringExtra("title");
        String desc = getIntent().getStringExtra("desc");
        String highlights = getIntent().getStringExtra("highlights");
        int imageRes = getIntent().getIntExtra("image", R.drawable.placeholder_image);
        String mapUrl = getIntent().getStringExtra("mapUrl");
        String extraUrl = getIntent().getStringExtra("extraUrl");
        String extraBtnText = getIntent().getStringExtra("extraBtnText");

        // Set UI content
        imageView.setImageResource(imageRes);
        titleView.setText(title);
        descView.setText(desc);
        highlightsView.setText(highlights);
        extraBtn.setText(extraBtnText);

        // Map button action
        mapBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mapUrl));
            intent.setPackage("com.google.android.apps.maps");
            startActivity(intent);
        });

        // Extra action button
        extraBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(extraUrl));
            startActivity(intent);
        });
    }
}
