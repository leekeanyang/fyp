package com.example.fyp.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.fyp.R;
import com.example.fyp.auth.LoginActivity;
import com.example.fyp.models.Report;
import com.example.fyp.repositories.EcoRepository;
import com.example.fyp.repositories.SiteCatalog;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminDashboardActivity extends AppCompatActivity {

    private ListView lvReports;
    private TextView tvRoyalBelumCapacity;
    private TextView tvKualaSepetangCapacity;
    private TextView tvAnalyticsReports;
    private TextView tvAnalyticsCapacity;
    private TextView tvReportBadge;
    private EditText etRoyalBelumCount;
    private EditText etRoyalBelumReason;
    private CircularProgressIndicator progressIndicator;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CoordinatorLayout coordinatorLayout;

    private EcoRepository ecoRepository;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference rootReference;
    private ReportListAdapter reportAdapter;
    private final List<Report> unresolvedReports = new ArrayList<>();
    private Report selectedReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        ecoRepository = new EcoRepository();
        ecoRepository.ensureSiteSeeds();
        firebaseAuth = FirebaseAuth.getInstance();
        rootReference = FirebaseDatabase.getInstance().getReference();

        coordinatorLayout = findViewById(R.id.coordinator_layout);
        lvReports = findViewById(R.id.lv_reports);
        tvRoyalBelumCapacity = findViewById(R.id.tv_royal_belum_capacity);
        tvKualaSepetangCapacity = findViewById(R.id.tv_kuala_sepetang_capacity);
        tvAnalyticsReports = findViewById(R.id.tv_analytics_reports);
        tvAnalyticsCapacity = findViewById(R.id.tv_analytics_capacity);
        tvReportBadge = findViewById(R.id.tv_report_badge);
        etRoyalBelumCount = findViewById(R.id.et_royal_belum_count);
        etRoyalBelumReason = findViewById(R.id.et_royal_belum_reason);
        progressIndicator = findViewById(R.id.progress_indicator);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);

        Button btnRefresh = findViewById(R.id.btn_refresh);
        Button btnDecrement = findViewById(R.id.btn_decrement_royal_belum);
        Button btnIncrement = findViewById(R.id.btn_increment_royal_belum);
        Button btnSet = findViewById(R.id.btn_set_royal_belum);
        Button btnLogout = findViewById(R.id.btn_logout);
        Button btnToggleAnalytics = findViewById(R.id.btn_toggle_analytics);
        Button btnResolveReports = findViewById(R.id.btn_resolve_reports);

        reportAdapter = new ReportListAdapter(unresolvedReports);
        lvReports.setAdapter(reportAdapter);
        lvReports.setOnItemClickListener((parent, view, position, id) -> {
            selectedReport = unresolvedReports.get(position);
            openResolveReport(selectedReport);
        });

        View analyticsContent = findViewById(R.id.ll_analytics_content);
        analyticsContent.setVisibility(View.GONE);
        tvReportBadge.setVisibility(View.GONE);
        progressIndicator.setVisibility(View.GONE);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadData();
            swipeRefreshLayout.setRefreshing(false);
        });

        btnRefresh.setOnClickListener(v -> {
            progressIndicator.setVisibility(View.VISIBLE);
            loadData();
        });
        btnDecrement.setOnClickListener(v -> adjustCount(-1));
        btnIncrement.setOnClickListener(v -> adjustCount(1));
        btnSet.setOnClickListener(v -> setCount());
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());
        btnResolveReports.setOnClickListener(v -> {
            Report report = selectedReport != null ? selectedReport : (!unresolvedReports.isEmpty() ? unresolvedReports.get(0) : null);
            if (report == null) {
                Toast.makeText(this, "No unresolved reports available.", Toast.LENGTH_SHORT).show();
                return;
            }
            openResolveReport(report);
        });
        btnToggleAnalytics.setOnClickListener(v -> {
            if (analyticsContent.getVisibility() == View.GONE) {
                analyticsContent.setVisibility(View.VISIBLE);
                btnToggleAnalytics.setText("Collapse Analytics");
            } else {
                analyticsContent.setVisibility(View.GONE);
                btnToggleAnalytics.setText("Expand Analytics");
            }
        });

        loadData();
    }

    private void loadData() {
        loadSiteCapacityCards();
        loadReports();
    }

    private void loadSiteCapacityCards() {
        ecoRepository.loadSiteCounts(new EcoRepository.SiteCountsListener() {
            @Override
            public void onLoaded(java.util.Map<String, Integer> siteCounts) {
                int royalBelumCount = siteCounts.containsKey(SiteCatalog.ROYAL_BELUM)
                        ? siteCounts.get(SiteCatalog.ROYAL_BELUM)
                        : SiteCatalog.getSiteMetaById(SiteCatalog.ROYAL_BELUM).getDefaultVisitorCount();
                int kualaSepetangCount = siteCounts.containsKey(SiteCatalog.KUALA_SEPETANG)
                        ? siteCounts.get(SiteCatalog.KUALA_SEPETANG)
                        : SiteCatalog.getSiteMetaById(SiteCatalog.KUALA_SEPETANG).getDefaultVisitorCount();

                int royalBelumMax = SiteCatalog.getSiteMetaById(SiteCatalog.ROYAL_BELUM).getMaxCapacity();
                int kualaSepetangMax = SiteCatalog.getSiteMetaById(SiteCatalog.KUALA_SEPETANG).getMaxCapacity();

                tvRoyalBelumCapacity.setText(String.format(Locale.getDefault(),
                        "Royal Belum: %d/%d (%s)",
                        royalBelumCount,
                        royalBelumMax,
                        EcoRepository.capacityLabel(royalBelumCount, royalBelumMax)));
                tvKualaSepetangCapacity.setText(String.format(Locale.getDefault(),
                        "Kuala Sepetang: %d/%d (%s)",
                        kualaSepetangCount,
                        kualaSepetangMax,
                        EcoRepository.capacityLabel(kualaSepetangCount, kualaSepetangMax)));
                etRoyalBelumCount.setText(String.valueOf(royalBelumCount));
            }

            @Override
            public void onError(String message) {
                showError(message);
            }
        });
    }

    private void loadReports() {
        ecoRepository.loadAllReports(new EcoRepository.ReportsListener() {
            @Override
            public void onLoaded(List<Report> reports) {
                unresolvedReports.clear();
                long litterCount = 0L;
                long wildlifeCount = 0L;
                long resolvedCount = 0L;

                for (Report report : reports) {
                    if ("Littering".equalsIgnoreCase(report.getIssueType())) {
                        litterCount++;
                    }
                    if ("Wildlife Disturbance".equalsIgnoreCase(report.getIssueType())) {
                        wildlifeCount++;
                    }
                    if ("Resolved".equalsIgnoreCase(report.getStatus())) {
                        resolvedCount++;
                    } else {
                        unresolvedReports.add(report);
                    }
                }

                tvReportBadge.setVisibility(unresolvedReports.isEmpty() ? View.GONE : View.VISIBLE);
                tvReportBadge.setText(String.valueOf(unresolvedReports.size()));
                tvAnalyticsReports.setText(String.format(Locale.getDefault(),
                        "Litter reports: %d, Wildlife disturbance: %d",
                        litterCount,
                        wildlifeCount));
                tvAnalyticsCapacity.setText(String.format(Locale.getDefault(),
                        "Resolved reports: %d, Pending review: %d",
                        resolvedCount,
                        unresolvedReports.size()));

                if (!unresolvedReports.isEmpty() && (selectedReport == null || "Resolved".equalsIgnoreCase(selectedReport.getStatus()))) {
                    selectedReport = unresolvedReports.get(0);
                }

                reportAdapter.notifyDataSetChanged();
                progressIndicator.setVisibility(View.GONE);
            }

            @Override
            public void onError(String message) {
                progressIndicator.setVisibility(View.GONE);
                showError(message);
            }
        });
    }

    private void adjustCount(int delta) {
        String countText = etRoyalBelumCount.getText().toString().trim();
        if (countText.isEmpty()) {
            etRoyalBelumCount.setError("Enter a number first.");
            return;
        }

        int currentCount = Integer.parseInt(countText);
        int newCount = Math.max(0, currentCount + delta);
        updateRoyalBelumCount(newCount);
    }

    private void setCount() {
        String countText = etRoyalBelumCount.getText().toString().trim();
        String reasonText = etRoyalBelumReason.getText().toString().trim();
        if (countText.isEmpty()) {
            etRoyalBelumCount.setError("Enter a count.");
            return;
        }
        if (reasonText.isEmpty()) {
            etRoyalBelumReason.setError("Please add a reason.");
            return;
        }

        int newCount = Integer.parseInt(countText);
        updateRoyalBelumCount(newCount);
        Snackbar.make(coordinatorLayout, "Royal Belum count updated: " + reasonText, Snackbar.LENGTH_SHORT).show();
        etRoyalBelumReason.setText("");
    }

    private void updateRoyalBelumCount(int newCount) {
        SiteCatalog.SiteMeta meta = SiteCatalog.getSiteMetaById(SiteCatalog.ROYAL_BELUM);
        if (meta == null) {
            return;
        }

        rootReference.child("sites").child(SiteCatalog.ROYAL_BELUM).runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                currentData.child("site_id").setValue(meta.getId());
                currentData.child("name").setValue(meta.getName());
                currentData.child("latitude").setValue(meta.getLatitude());
                currentData.child("longitude").setValue(meta.getLongitude());
                currentData.child("max_capacity").setValue(meta.getMaxCapacity());
                currentData.child("qr_payload").setValue(SiteCatalog.buildQrPayload(meta.getId()));
                currentData.child("current_visitors").setValue(newCount);
                currentData.child("status").setValue(EcoRepository.capacityLabel(newCount, meta.getMaxCapacity()));
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                if (error != null) {
                    showError(error.getMessage());
                    return;
                }

                if (committed) {
                    loadSiteCapacityCards();
                }
            }
        });
    }

    private void openResolveReport(Report report) {
        Intent intent = new Intent(this, ResolveReportActivity.class);
        intent.putExtra("reportId", report.getId());
        startActivity(intent);
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to log out of the admin dashboard?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    firebaseAuth.signOut();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void showError(String message) {
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }

    private class ReportListAdapter extends ArrayAdapter<Report> {
        ReportListAdapter(List<Report> reports) {
            super(AdminDashboardActivity.this, android.R.layout.simple_list_item_2, reports);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View row = convertView;
            if (row == null) {
                row = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            }

            Report report = getItem(position);
            TextView title = row.findViewById(android.R.id.text1);
            TextView subtitle = row.findViewById(android.R.id.text2);

            if (report != null) {
                title.setText(report.getIssueType() + " at " + report.getSite());
                subtitle.setText(report.getStatus() + " | " + report.getLocation());
            } else {
                title.setText("No report");
                subtitle.setText("");
            }

            return row;
        }
    }
}
