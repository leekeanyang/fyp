package com.example.fyp.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fyp.R;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class RangerAdminActivity extends AppCompatActivity {

    RecyclerView recyclerReports;
    ReportsAdapter adapter;
    List<Report> reportList = new ArrayList<>();
    DatabaseReference reportsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranger_admin);

        recyclerReports = findViewById(R.id.recyclerReports);
        recyclerReports.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReportsAdapter(reportList);
        recyclerReports.setAdapter(adapter);

        reportsRef = FirebaseDatabase.getInstance().getReference("EcoReports");
        loadReports();
    }

    private void loadReports() {
        reportsRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                reportList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Report r = snap.getValue(Report.class);
                    if (r != null) reportList.add(r);
                }
                adapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // Adapter + ViewHolder
    class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.ReportVH> {

        List<Report> list;
        ReportsAdapter(List<Report> list) { this.list = list; }

        @NonNull
        @Override
        public ReportVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report_admin, parent, false);
            return new ReportVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ReportVH holder, int position) {
            Report r = list.get(position);
            holder.txtIssue.setText(r.issue);
            holder.txtDesc.setText(r.description);
            holder.txtLocation.setText("Lat: " + r.latitude + ", Lng: " + r.longitude);
            holder.txtStatus.setText("Status: " + r.status);
            if (r.photoUrl != null && !r.photoUrl.isEmpty()) {
                Glide.with(RangerAdminActivity.this).load(r.photoUrl).into(holder.imgPhoto);
            } else {
                holder.imgPhoto.setImageResource(R.drawable.ic_placeholder);
            }

            holder.btnUpdateStatus.setOnClickListener(v -> showStatusOptions(r));
        }

        @Override public int getItemCount() { return list.size(); }

        class ReportVH extends RecyclerView.ViewHolder {
            TextView txtIssue, txtDesc, txtLocation, txtStatus;
            ImageView imgPhoto;
            Button btnUpdateStatus;
            ReportVH(@NonNull View itemView) {
                super(itemView);
                txtIssue = itemView.findViewById(R.id.txtIssue);
                txtDesc = itemView.findViewById(R.id.txtDesc);
                txtLocation = itemView.findViewById(R.id.txtLocation);
                txtStatus = itemView.findViewById(R.id.txtStatus);
                imgPhoto = itemView.findViewById(R.id.imgPhoto);
                btnUpdateStatus = itemView.findViewById(R.id.btnUpdateStatus);
            }
        }
    }

    private void showStatusOptions(Report report) {
        final String[] statuses = {"Submitted", "Reviewed", "Resolved"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Update status")
                .setSingleChoiceItems(statuses, -1, (dialog, which) -> {
                    String newStatus = statuses[which];
                    reportsRef.child(report.id).child("status").setValue(newStatus);
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .show();
    }

    // Report model (same as earlier)
    public static class Report {
        public String id, issue, description, photoUrl, status, userId;
        public double latitude, longitude;
        public long timestamp;
        public Report() {}
        public Report(String id, String issue, String description, double latitude, double longitude, String photoUrl, String status, String userId, long timestamp) {
            this.id = id;
            this.issue = issue;
            this.description = description;
            this.latitude = latitude;
            this.longitude = longitude;
            this.photoUrl = photoUrl;
            this.status = status;
            this.userId = userId;
            this.timestamp = timestamp;
        }
    }
}

