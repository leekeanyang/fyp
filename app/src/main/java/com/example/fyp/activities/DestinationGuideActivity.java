package com.example.fyp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.fyp.R;

public class DestinationGuideActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination_guide);

        // Royal Belum
        CardView royalBelum = findViewById(R.id.royalBelumCard);
        royalBelum.setOnClickListener(v -> {
            Intent intent = new Intent(DestinationGuideActivity.this, RoyalBelumActivity.class);
            startActivity(intent);
        });

        // Gua Tempurung
        CardView guaTempurung = findViewById(R.id.guaTempurungCard);
        guaTempurung.setOnClickListener(v -> {
            Intent intent = new Intent(DestinationGuideActivity.this, GuaTempurungActivity.class);
            startActivity(intent);
        });

        // Kuala Sepetang
        CardView kualaSepetang = findViewById(R.id.kualaSepetangCard);
        kualaSepetang.setOnClickListener(v -> {
            Intent intent = new Intent(DestinationGuideActivity.this, KualaSepetangActivity.class);
            startActivity(intent);
        });
    }
}
