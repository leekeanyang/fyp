package com.example.fyp.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fyp.R;
import com.example.fyp.models.EcoReport;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdminDashboardActivity extends AppCompatActivity {
    ListView listReports;
    DatabaseReference reportsRef;
    ArrayList<String> reportsList = new ArrayList<>();
    ArrayList<EcoReport> reportObjects = new ArrayList<>();
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        listReports = findViewById(R.id.list_reports);
        reportsRef = FirebaseDatabase.getInstance().getReference("reports");

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, reportsList);
        listReports.setAdapter(adapter);

        // Fetch reports
        reportsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                reportsList.clear();
                reportObjects.clear();

                for (DataSnapshot data : snapshot.getChildren()) {
                    EcoReport report = data.getValue(EcoReport.class);
                    if (report != null) {
                        reportsList.add("📌 " + report.issueType + " - Status: " + statusToText(report.status));
                        reportObjects.add(report);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(AdminDashboardActivity.this, "Error loading reports", Toast.LENGTH_SHORT).show();
            }
        });

        // When clicked, allow ranger to update status
        listReports.setOnItemClickListener((parent, view, position, id) -> {
            EcoReport report = reportObjects.get(position);

            // Cycle through status (1→2→3)
            int newStatus = report.status < 3 ? report.status + 1 : 3;
            reportsRef.child(report.id).child("status").setValue(newStatus);
            Toast.makeText(this, "Updated to: " + statusToText(newStatus), Toast.LENGTH_SHORT).show();
        });
    }

    private String statusToText(int status) {
        switch (status) {
            case 1: return "Pending";
            case 2: return "In Review";
            case 3: return "Resolved";
            default: return "Unknown";
        }
    }
}
