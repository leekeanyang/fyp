package com.example.fyp.activities;

import org.osmdroid.views.MapView;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ORSRouteTask extends AsyncTask<Void, Void, List<GeoPoint>> {
    private final double startLat, startLon, endLat, endLon;
    private final MapView mapView;
    private final String API_KEY = "eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6IjdjYWYwZTdiNzQ1ZTQyMTRiY2E1M2I0NjExMTY3MGUzIiwiaCI6Im11cm11cjY0In0=";

    public ORSRouteTask(double startLat, double startLon, double endLat, double endLon, MapView mapView) {
        this.startLat = startLat;
        this.startLon = startLon;
        this.endLat = endLat;
        this.endLon = endLon;
        this.mapView = mapView;
    }

    @Override
    protected List<GeoPoint> doInBackground(Void... voids) {
        List<GeoPoint> points = new ArrayList<>();
        try {
            String urlStr = "https://api.openrouteservice.org/v2/directions/foot-walking?api_key="
                    + API_KEY + "&start=" + startLon + "," + startLat + "&end=" + endLon + "," + endLat;
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();

            JSONObject json = new JSONObject(sb.toString());
            JSONArray coords = json.getJSONArray("features")
                    .getJSONObject(0)
                    .getJSONObject("geometry")
                    .getJSONArray("coordinates");

            for (int i = 0; i < coords.length(); i++) {
                JSONArray coord = coords.getJSONArray(i);
                points.add(new GeoPoint(coord.getDouble(1), coord.getDouble(0)));
            }

        } catch (Exception e) {
            Log.e("ORSRouteTask", "Error fetching route", e);
        }
        return points;
    }

    @Override
    protected void onPostExecute(List<GeoPoint> geoPoints) {
        if (!geoPoints.isEmpty()) {
            Polyline polyline = new Polyline();
            polyline.setPoints(geoPoints);
            polyline.setColor(0xFF388E3C);
            polyline.setWidth(8f);
            mapView.getOverlays().add(polyline);
            mapView.invalidate();
        }
    }
}

