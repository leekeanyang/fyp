package com.example.fyp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fyp.R;
import com.example.fyp.models.Site;

public class SiteDetailActivity extends AppCompatActivity {

    ImageView imgDetail;
    TextView txtTitle, txtLongDesc;
    Button btnOpenMap, btnReportIssue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_detail);

        imgDetail = findViewById(R.id.imgDetail);
        txtTitle = findViewById(R.id.txtTitle);
        txtLongDesc = findViewById(R.id.txtLongDesc);
        btnOpenMap = findViewById(R.id.btnOpenMap);
        btnReportIssue = findViewById(R.id.btnReportIssue);

        Site site = (Site) getIntent().getSerializableExtra("site");
        if (site != null) {
            imgDetail.setImageResource(site.getImageResId());
            txtTitle.setText(site.getName());
            txtLongDesc.setText(site.getLongDesc());

            btnOpenMap.setOnClickListener(v -> {
                // Open external maps app or your internal map activity
                String uri = "geo:" + site.getLat() + "," + site.getLng() + "?q=" + Uri.encode(site.getName());
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {
                    // fallback to generic geo intent
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
                }
            });

            btnReportIssue.setOnClickListener(v -> {
                // Launch your EcoReportActivity and optionally prefill location/site
                Intent i = new Intent(this, EcoImpactReportActivity.class);
                i.putExtra("siteName", site.getName());
                i.putExtra("lat", site.getLat());
                i.putExtra("lng", site.getLng());
                startActivity(i);
            });
        }
    }
}

