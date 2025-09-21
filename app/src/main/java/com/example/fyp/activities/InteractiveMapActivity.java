package com.example.fyp.activities;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.caverock.androidsvg.BuildConfig;
import com.example.fyp.R;
import com.example.fyp.models.Destination;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

public class InteractiveMapActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;
    private MapView map;
    private Spinner itinerarySpinner;
    private List<Destination> destinations;
    private List<Marker> aiMarkers;

    private FusedLocationProviderClient fusedLocationClient;
    private GeoPoint userLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        setContentView(R.layout.activity_interactive_map);

        map = findViewById(R.id.map);
        map.setMultiTouchControls(true);

        itinerarySpinner = findViewById(R.id.itinerary_spinner);
        Button planBtn = findViewById(R.id.plan_itinerary_button);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        destinations = new ArrayList<>();
        aiMarkers = new ArrayList<>();

        // Sample destinations
        destinations.add(new Destination("Royal Belum", 5.9466, 100.9594, "Rainforest/ecosystem tourism", "Diverse flora/fauna"));
        destinations.add(new Destination("Gua Tempurung", 4.4148, 101.1878, "Cave-based tourism", "Limestone caves"));
        destinations.add(new Destination("Kuala Sepetang", 4.8371, 100.6267, "Mangrove ecosystem", "Firefly watching"));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                getDestinationNames()
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        itinerarySpinner.setAdapter(adapter);

        addMarkers();
        map.getController().setZoom(8.0);
        map.getController().setCenter(new GeoPoint(4.9, 100.9));

        planBtn.setOnClickListener(v -> planItinerary());

        FloatingActionButton myLocationBtn = findViewById(R.id.fab_my_location);
        FloatingActionButton filterSitesBtn = findViewById(R.id.fab_filter_sites);
        FloatingActionButton resetViewBtn = findViewById(R.id.fab_reset_view);

        myLocationBtn.setOnClickListener(v -> {
            if (userLocation != null) {
                map.getController().setZoom(14.0);
                map.getController().animateTo(userLocation);
                Toast.makeText(this, "Centered on your location", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "User location not available", Toast.LENGTH_SHORT).show();
            }
        });

        filterSitesBtn.setOnClickListener(v ->
                Toast.makeText(this, "Filter feature coming soon", Toast.LENGTH_SHORT).show()
        );

        resetViewBtn.setOnClickListener(v -> resetMapView());

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        } else {
            startLocationUpdates();
        }

        // Example: Add AI marker with pulsing effect
        addAIMarker(new Destination("AI Suggested Site", 5.0, 101.0, "AI Recommendation", "Pulsing marker until clicked"));
    }

    private List<String> getDestinationNames() {
        List<String> names = new ArrayList<>();
        for (Destination d : destinations) {
            names.add(d.getName());
        }
        return names;
    }

    private void addMarkers() {
        for (Destination d : destinations) {
            Marker marker = new Marker(map);
            marker.setPosition(new GeoPoint(d.getLatitude(), d.getLongitude()));
            marker.setTitle(d.getName());
            marker.setSnippet(d.getHighlight());
            marker.setOnMarkerClickListener((m, mapView) -> {
                map.getController().setZoom(13.0);
                map.getController().animateTo(m.getPosition());
                Toast.makeText(this, m.getTitle() + " - " + m.getSnippet(), Toast.LENGTH_SHORT).show();
                return true;
            });
            map.getOverlays().add(marker);
        }
    }

    private void planItinerary() {
        Object selectedObj = itinerarySpinner.getSelectedItem();
        if (selectedObj == null) {
            Toast.makeText(this, "Please select a destination", Toast.LENGTH_SHORT).show();
            return;
        }
        String selectedSite = selectedObj.toString();

        Destination selectedDestination = null;
        for (Destination d : destinations) {
            if (d.getName().equals(selectedSite)) {
                selectedDestination = d;
                break;
            }
        }

        if (selectedDestination != null && userLocation != null) {
            Polyline route = new Polyline();
            List<GeoPoint> points = new ArrayList<>();
            points.add(userLocation);
            points.add(new GeoPoint(selectedDestination.getLatitude(), selectedDestination.getLongitude()));
            route.setPoints(points);
            map.getOverlays().add(route);
            map.invalidate();
            Toast.makeText(this, "Route planned to " + selectedDestination.getName(), Toast.LENGTH_SHORT).show();
        } else if (userLocation == null) {
            Toast.makeText(this, "User location not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult.getLastLocation() != null) {
                    userLocation = new GeoPoint(locationResult.getLastLocation().getLatitude(),
                            locationResult.getLastLocation().getLongitude());
                    map.getController().setCenter(userLocation);
                    checkNearbySites();
                }
            }
        }, getMainLooper());
    }

    private void checkNearbySites() {
        if (userLocation == null) return;

        double radiusMeters = 10000;
        for (Destination d : destinations) {
            double distance = userLocation.distanceToAsDouble(new GeoPoint(d.getLatitude(), d.getLongitude()));
            if (distance <= radiusMeters) {
                Toast.makeText(this, "Nearby site: " + d.getName(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void resetMapView() {
        map.getController().setZoom(8.0);
        map.getController().animateTo(new GeoPoint(4.9, 100.9));
        Toast.makeText(this, "Map reset to default view", Toast.LENGTH_SHORT).show();
    }

    // -------------------- AI Marker Pulsing --------------------
    private void addAIMarker(Destination d) {
        Marker marker = new Marker(map);
        marker.setPosition(new GeoPoint(d.getLatitude(), d.getLongitude()));
        marker.setTitle(d.getName());
        marker.setSnippet(d.getHighlight());
        marker.setIcon(getResources().getDrawable(R.drawable.ic_active_point));

        map.getOverlays().add(marker);
        aiMarkers.add(marker);

        pulseMarkerContinuous(marker);
    }

    private void pulseMarkerContinuous(final Marker marker) {
        if (marker == null) return;

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(marker, "scaleX", 1f, 1.5f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(marker, "scaleY", 1f, 1.5f, 1f);

        scaleX.setDuration(800);
        scaleY.setDuration(800);

        scaleX.setRepeatCount(ValueAnimator.INFINITE);
        scaleY.setRepeatCount(ValueAnimator.INFINITE);

        scaleX.start();
        scaleY.start();

        marker.setOnMarkerClickListener((m, mapView) -> {
            scaleX.cancel();
            scaleY.cancel();
            map.getController().setZoom(13.0);
            map.getController().animateTo(m.getPosition());
            Toast.makeText(this, m.getTitle() + " - " + m.getSnippet(), Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}
