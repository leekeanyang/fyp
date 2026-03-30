package com.example.fyp.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.fyp.R;
import com.example.fyp.models.Destination;
import com.example.fyp.repositories.AiItineraryPlanner;
import com.example.fyp.repositories.EcoRepository;
import com.example.fyp.repositories.SiteCatalog;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class InteractiveMapActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;
    private static final String TRIP_PREFS = "TripPlannerPrefs";

    private MapView map;
    private Spinner itinerarySpinner;
    private Spinner interestSpinner;
    private Spinner timeSpinner;
    private Spinner seasonSpinner;
    private TextView itineraryResult;
    private Button btnSendToEcoTrack;

    private final Map<String, Integer> liveSiteCounts = new HashMap<>();
    private List<Destination> destinations = new ArrayList<>();
    private Polyline currentRoute;
    private GeoPoint userLocation;

    private FusedLocationProviderClient fusedLocationClient;
    private EcoRepository ecoRepository;
    private AiItineraryPlanner aiItineraryPlanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_interactive_map);

        map = findViewById(R.id.map);
        map.setMultiTouchControls(true);

        itinerarySpinner = findViewById(R.id.itinerary_spinner);
        interestSpinner = findViewById(R.id.interest_spinner);
        timeSpinner = findViewById(R.id.time_spinner);
        seasonSpinner = findViewById(R.id.season_spinner);
        itineraryResult = findViewById(R.id.itinerary_result);
        btnSendToEcoTrack = findViewById(R.id.btn_send_to_ecotrack);
        Button planBtn = findViewById(R.id.plan_itinerary_button);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        ecoRepository = new EcoRepository();
        ecoRepository.ensureSiteSeeds();
        aiItineraryPlanner = new AiItineraryPlanner();

        setupSpinners();
        configureMapDefaults();
        loadLiveSiteCounts();

        planBtn.setOnClickListener(v -> planItinerary());
        btnSendToEcoTrack.setOnClickListener(v -> openEcoTrack());

        FloatingActionButton myLocationBtn = findViewById(R.id.fab_my_location);
        FloatingActionButton filterSitesBtn = findViewById(R.id.fab_filter_sites);
        FloatingActionButton resetViewBtn = findViewById(R.id.fab_reset_view);

        myLocationBtn.setOnClickListener(v -> {
            if (userLocation != null) {
                map.getController().setZoom(14.0);
                map.getController().animateTo(userLocation);
            } else {
                Toast.makeText(this, "Waiting for GPS location...", Toast.LENGTH_SHORT).show();
            }
        });

        filterSitesBtn.setOnClickListener(v -> showLowImpactAlternatives());
        resetViewBtn.setOnClickListener(v -> resetMapView());

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        } else {
            requestLastKnownLocation();
        }
    }

    private void setupSpinners() {
        itinerarySpinner.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                SiteCatalog.getSiteNames(false)));

        interestSpinner.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Wildlife", "Adventure", "Community", "Photography", "Relaxed sightseeing"}));

        timeSpinner.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"2-3 hours", "Half day", "Full day"}));

        seasonSpinner.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Dry season", "Wet season", "Flexible"}));
    }

    private void configureMapDefaults() {
        map.getController().setZoom(9.0);
        map.getController().setCenter(new GeoPoint(4.9, 101.1));
    }

    private void loadLiveSiteCounts() {
        ecoRepository.loadSiteCounts(new EcoRepository.SiteCountsListener() {
            @Override
            public void onLoaded(Map<String, Integer> siteCounts) {
                liveSiteCounts.clear();
                liveSiteCounts.putAll(siteCounts);
                destinations = SiteCatalog.getDestinations(liveSiteCounts);
                renderMarkers();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(InteractiveMapActivity.this, message, Toast.LENGTH_SHORT).show();
                destinations = SiteCatalog.getDestinations(null);
                renderMarkers();
            }
        });
    }

    private void renderMarkers() {
        map.getOverlays().clear();
        for (Destination destination : destinations) {
            Marker marker = new Marker(map);
            marker.setPosition(new GeoPoint(destination.getLatitude(), destination.getLongitude()));
            marker.setTitle(destination.getName());
            marker.setSnippet(String.format(Locale.getDefault(),
                    "Capacity: %s (%d/%d)\n%s",
                    destination.getCapacityStatus(),
                    destination.getCurrentVisitorCount(),
                    destination.getMaxCapacity(),
                    destination.getHighlight()));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            map.getOverlays().add(marker);
        }

        if (currentRoute != null) {
            map.getOverlays().add(currentRoute);
        }
        map.invalidate();
    }

    private void requestLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                userLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
            }
        });
    }

    private void planItinerary() {
        String selectedSiteName = String.valueOf(itinerarySpinner.getSelectedItem());
        String siteId = SiteCatalog.getSiteId(selectedSiteName);
        SiteCatalog.SiteMeta meta = SiteCatalog.getSiteMetaById(siteId);
        if (meta == null) {
            Toast.makeText(this, "Please select a destination.", Toast.LENGTH_SHORT).show();
            return;
        }

        drawRouteTo(meta);

        int visitorCount = liveSiteCounts.containsKey(siteId)
                ? liveSiteCounts.get(siteId)
                : meta.getDefaultVisitorCount();
        String interest = String.valueOf(interestSpinner.getSelectedItem());
        String availableTime = String.valueOf(timeSpinner.getSelectedItem());
        String season = String.valueOf(seasonSpinner.getSelectedItem());

        aiItineraryPlanner.buildPlan(this, meta, interest, availableTime, season, visitorCount, planText -> {
            itineraryResult.setText(planText);
            rememberPlan(meta, interest, availableTime, season, planText);
            Toast.makeText(this, "Itinerary updated.", Toast.LENGTH_SHORT).show();
        });
    }

    private void drawRouteTo(SiteCatalog.SiteMeta meta) {
        if (currentRoute != null) {
            map.getOverlays().remove(currentRoute);
        }

        currentRoute = new Polyline();
        List<GeoPoint> points = new ArrayList<>();
        GeoPoint destinationPoint = new GeoPoint(meta.getLatitude(), meta.getLongitude());
        if (userLocation != null) {
            points.add(userLocation);
        }
        points.add(destinationPoint);
        currentRoute.setPoints(points);
        currentRoute.setColor(Color.parseColor("#2E7D32"));
        currentRoute.setWidth(10f);
        map.getOverlays().add(currentRoute);

        map.getController().animateTo(destinationPoint);
        map.invalidate();
    }

    private void rememberPlan(SiteCatalog.SiteMeta meta,
                              String interest,
                              String availableTime,
                              String season,
                              String planText) {
        float distanceKm = 0f;
        if (userLocation != null) {
            float[] result = new float[1];
            android.location.Location.distanceBetween(
                    userLocation.getLatitude(),
                    userLocation.getLongitude(),
                    meta.getLatitude(),
                    meta.getLongitude(),
                    result
            );
            distanceKm = result[0] / 1000f;
        }

        getSharedPreferences(TRIP_PREFS, MODE_PRIVATE)
                .edit()
                .putString("selected_site_name", meta.getName())
                .putString("selected_site_id", meta.getId())
                .putString("interest", interest)
                .putString("available_time", availableTime)
                .putString("season", season)
                .putString("itinerary_plan", planText)
                .putFloat("route_distance_km", distanceKm)
                .apply();
    }

    private void openEcoTrack() {
        SharedPreferences prefs = getSharedPreferences(TRIP_PREFS, MODE_PRIVATE);
        Intent intent = new Intent(this, EcoTrackActivity.class);
        intent.putExtra("siteName", prefs.getString("selected_site_name", ""));
        intent.putExtra("routeDistanceKm", prefs.getFloat("route_distance_km", 0f));
        intent.putExtra("itineraryPlan", prefs.getString("itinerary_plan", ""));
        startActivity(intent);
    }

    private void showLowImpactAlternatives() {
        StringBuilder builder = new StringBuilder("Lower-crowd alternatives:\n");
        for (SiteCatalog.SiteMeta meta : SiteCatalog.getSiteMetas()) {
            int visitors = liveSiteCounts.containsKey(meta.getId())
                    ? liveSiteCounts.get(meta.getId())
                    : meta.getDefaultVisitorCount();
            if ("Low".equals(EcoRepository.capacityLabel(visitors, meta.getMaxCapacity()))) {
                builder.append("- ").append(meta.getName()).append('\n');
            }
        }
        itineraryResult.setText(builder.toString().trim());
    }

    private void resetMapView() {
        if (currentRoute != null) {
            map.getOverlays().remove(currentRoute);
            currentRoute = null;
        }
        configureMapDefaults();
        renderMarkers();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestLastKnownLocation();
        }
    }
}
