package com.example.fyp.repositories;

import androidx.annotation.NonNull;

import com.example.fyp.models.CheckInRecord;
import com.example.fyp.models.Report;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EcoRepository {

    public interface CompletionListener {
        void onSuccess();
        void onError(String message);
    }

    public interface ReportListener {
        void onLoaded(Report report);
        void onError(String message);
    }

    public interface ReportsListener {
        void onLoaded(List<Report> reports);
        void onError(String message);
    }

    public interface SiteCountsListener {
        void onLoaded(Map<String, Integer> siteCounts);
        void onError(String message);
    }

    public interface CheckInListener {
        void onCheckedIn(CheckInRecord record, int currentCount, String capacityLabel);
        void onError(String message);
    }

    private final DatabaseReference rootReference;

    public EcoRepository() {
        rootReference = FirebaseDatabase.getInstance().getReference();
    }

    public DatabaseReference getReportReference(String reportId) {
        return rootReference.child("reports").child(reportId);
    }

    public String createReportId() {
        String key = rootReference.child("reports").push().getKey();
        return key != null ? key : "report_" + System.currentTimeMillis();
    }

    public void ensureSiteSeeds() {
        for (SiteCatalog.SiteMeta meta : SiteCatalog.getSiteMetas()) {
            DatabaseReference siteReference = rootReference.child("sites").child(meta.getId());
            siteReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Map<String, Object> defaults = new HashMap<>();
                    if (!snapshot.hasChild("site_id")) {
                        defaults.put("site_id", meta.getId());
                    }
                    if (!snapshot.hasChild("name")) {
                        defaults.put("name", meta.getName());
                    }
                    if (!snapshot.hasChild("latitude")) {
                        defaults.put("latitude", meta.getLatitude());
                    }
                    if (!snapshot.hasChild("longitude")) {
                        defaults.put("longitude", meta.getLongitude());
                    }
                    if (!snapshot.hasChild("max_capacity")) {
                        defaults.put("max_capacity", meta.getMaxCapacity());
                    }
                    if (!snapshot.hasChild("current_visitors")) {
                        defaults.put("current_visitors", meta.getDefaultVisitorCount());
                    }
                    if (!snapshot.hasChild("status")) {
                        defaults.put("status", capacityLabel(meta.getDefaultVisitorCount(), meta.getMaxCapacity()));
                    }
                    if (!snapshot.hasChild("qr_payload")) {
                        defaults.put("qr_payload", SiteCatalog.buildQrPayload(meta.getId()));
                    }
                    if (!defaults.isEmpty()) {
                        siteReference.updateChildren(defaults);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Best-effort seeding only.
                }
            });
        }
    }

    public void submitReport(Report report, CompletionListener listener) {
        if (report.getId() == null || report.getId().trim().isEmpty()) {
            report.setId(createReportId());
        }
        if (report.getStatus() == null || report.getStatus().trim().isEmpty()) {
            report.setStatus("Submitted");
        }
        if (report.getTimestamp() == 0L) {
            report.setTimestamp(System.currentTimeMillis());
        }
        report.setUpdatedAt(report.getUpdatedAt() == 0L ? report.getTimestamp() : report.getUpdatedAt());

        getReportReference(report.getId()).setValue(report)
                .addOnSuccessListener(unused -> listener.onSuccess())
                .addOnFailureListener(error -> listener.onError(error.getMessage()));
    }

    public void loadLatestReportForUser(String userId, ReportListener listener) {
        rootReference.child("reports")
                .orderByChild("submittedByUserId")
                .equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Report latest = null;
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Report report = child.getValue(Report.class);
                            if (report == null) {
                                continue;
                            }
                            if (report.getId() == null || report.getId().isEmpty()) {
                                report.setId(child.getKey());
                            }
                            if (latest == null || report.getTimestamp() > latest.getTimestamp()) {
                                latest = report;
                            }
                        }

                        if (latest != null) {
                            listener.onLoaded(latest);
                        } else {
                            listener.onError("No reports found for this user.");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.onError(error.getMessage());
                    }
                });
    }

    public void loadAllReports(ReportsListener listener) {
        rootReference.child("reports").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Report> reports = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Report report = child.getValue(Report.class);
                    if (report == null) {
                        continue;
                    }
                    if (report.getId() == null || report.getId().isEmpty()) {
                        report.setId(child.getKey());
                    }
                    reports.add(report);
                }
                Collections.sort(reports, Comparator.comparingLong(Report::getTimestamp).reversed());
                listener.onLoaded(reports);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.getMessage());
            }
        });
    }

    public void updateReportStatus(String reportId, String status, String adminAction, String adminName, CompletionListener listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        updates.put("adminAction", adminAction);
        updates.put("adminName", adminName);
        updates.put("updatedAt", System.currentTimeMillis());

        getReportReference(reportId).updateChildren(updates)
                .addOnSuccessListener(unused -> listener.onSuccess())
                .addOnFailureListener(error -> listener.onError(error.getMessage()));
    }

    public void loadSiteCounts(SiteCountsListener listener) {
        rootReference.child("sites").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Integer> counts = new HashMap<>();
                for (SiteCatalog.SiteMeta meta : SiteCatalog.getSiteMetas()) {
                    DataSnapshot siteSnapshot = snapshot.child(meta.getId());
                    Integer value = siteSnapshot.child("current_visitors").getValue(Integer.class);
                    counts.put(meta.getId(), value != null ? value : meta.getDefaultVisitorCount());
                }
                listener.onLoaded(counts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.getMessage());
            }
        });
    }

    public void recordCheckIn(String siteId,
                              String userId,
                              String userEmail,
                              String method,
                              String qrPayload,
                              CheckInListener listener) {
        SiteCatalog.SiteMeta meta = SiteCatalog.getSiteMetaById(siteId);
        if (meta == null) {
            listener.onError("Unknown site selected for check-in.");
            return;
        }

        DatabaseReference siteReference = rootReference.child("sites").child(meta.getId());
        siteReference.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Integer currentVisitors = currentData.child("current_visitors").getValue(Integer.class);
                int newCount = (currentVisitors != null ? currentVisitors : meta.getDefaultVisitorCount()) + 1;
                currentData.child("site_id").setValue(meta.getId());
                currentData.child("name").setValue(meta.getName());
                currentData.child("latitude").setValue(meta.getLatitude());
                currentData.child("longitude").setValue(meta.getLongitude());
                currentData.child("max_capacity").setValue(meta.getMaxCapacity());
                currentData.child("qr_payload").setValue(SiteCatalog.buildQrPayload(meta.getId()));
                currentData.child("current_visitors").setValue(newCount);
                currentData.child("status").setValue(capacityLabel(newCount, meta.getMaxCapacity()));
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                if (error != null) {
                    listener.onError(error.getMessage());
                    return;
                }

                if (!committed || currentData == null) {
                    listener.onError("Check-in was not recorded.");
                    return;
                }

                Integer updatedCount = currentData.child("current_visitors").getValue(Integer.class);
                int currentCount = updatedCount != null ? updatedCount : meta.getDefaultVisitorCount();
                String recordId = rootReference.child("checkins").push().getKey();
                CheckInRecord record = new CheckInRecord(
                        recordId != null ? recordId : "checkin_" + System.currentTimeMillis(),
                        meta.getId(),
                        meta.getName(),
                        userId,
                        userEmail,
                        method,
                        qrPayload,
                        System.currentTimeMillis()
                );

                rootReference.child("checkins").child(record.getId()).setValue(record)
                        .addOnSuccessListener(unused -> listener.onCheckedIn(record, currentCount, capacityLabel(currentCount, meta.getMaxCapacity())))
                        .addOnFailureListener(writeError -> listener.onError(writeError.getMessage()));
            }
        });
    }

    public static String capacityLabel(int currentVisitors, int maxCapacity) {
        if (maxCapacity <= 0) {
            return "Unknown";
        }

        double ratio = currentVisitors / (double) maxCapacity;
        if (ratio < 0.5d) {
            return "Low";
        }
        if (ratio < 0.8d) {
            return "Moderate";
        }
        return "High";
    }

    public static String buildLocationLabel(double latitude, double longitude) {
        return String.format(Locale.getDefault(), "Lat %.5f, Lng %.5f", latitude, longitude);
    }
}
