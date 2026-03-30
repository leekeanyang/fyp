package com.example.fyp.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp.R;
import com.example.fyp.adapters.SiteAdapter;
import com.example.fyp.models.Site;
import com.example.fyp.repositories.SiteCatalog;

import java.util.ArrayList;
import java.util.List;

public class DestinationGuideActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SiteAdapter adapter;
    private List<Site> siteList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination_guide);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Destination Guide");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        recyclerView = findViewById(R.id.recyclerViewDestinations);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        siteList = new ArrayList<>(SiteCatalog.getGuideSites());
        adapter = new SiteAdapter(this, siteList);
        recyclerView.setAdapter(adapter);
    }
}
