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

import java.util.Locale;

public class SiteDetailActivity extends AppCompatActivity {

    private ImageView imgDetail;
    private TextView txtTitle, txtLongDesc, txtHighlights, txtGuidelines, txtWarnings;
    private Button btnOpenMap, btnReportIssue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_detail);

        imgDetail = findViewById(R.id.imgDetail);
        txtTitle = findViewById(R.id.txtTitle);
        txtLongDesc = findViewById(R.id.txtLongDesc);
        txtHighlights = findViewById(R.id.txtHighlights);
        txtGuidelines = findViewById(R.id.txtGuidelines);
        txtWarnings = findViewById(R.id.txtWarnings);
        btnOpenMap = findViewById(R.id.btnOpenMap);
        btnReportIssue = findViewById(R.id.btnReportIssue);

        Site site = (Site) getIntent().getSerializableExtra("site");
        if (site != null) {
            imgDetail.setImageResource(site.getImageResId());
            txtTitle.setText(site.getName());
            txtLongDesc.setText(site.getLongDescription());
            txtHighlights.setText(site.getHighlights());
            txtGuidelines.setText(site.getGuidelines());
            txtWarnings.setText(site.getWarnings());

            btnOpenMap.setOnClickListener(v -> {
                String uri = String.format(Locale.getDefault(), "geo:%f,%f?q=%s", 
                        site.getLat(), site.getLng(), Uri.encode(site.getName()));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
                }
            });

            btnReportIssue.setOnClickListener(v -> {
                Intent i = new Intent(this, EcoImpactReportActivity.class);
                i.putExtra("siteName", site.getName());
                i.putExtra("lat", site.getLat());
                i.putExtra("lng", site.getLng());
                startActivity(i);
            });
        }
    }
}
