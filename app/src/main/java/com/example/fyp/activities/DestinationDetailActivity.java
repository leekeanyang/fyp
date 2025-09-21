package com.example.fyp.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fyp.R;

public class DestinationDetailActivity extends AppCompatActivity {

    private TextView nameTextView, descriptionTextView, guidelinesTextView;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination_detail);

        nameTextView = findViewById(R.id.dest_name);
        descriptionTextView = findViewById(R.id.dest_description);
        guidelinesTextView = findViewById(R.id.dest_guidelines);
        backButton = findViewById(R.id.back_button);

        // Get data from Intent
        String name = getIntent().getStringExtra("name");
        String description = getIntent().getStringExtra("description");
        String guidelines = getIntent().getStringExtra("guidelines");

        // Set to TextViews
        nameTextView.setText(name);
        descriptionTextView.setText(description);
        guidelinesTextView.setText("Guidelines: " + guidelines);

        backButton.setOnClickListener(v -> finish());
    }
}
