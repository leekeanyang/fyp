package com.example.fyp.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp.R;
import com.example.fyp.adapters.AdminLogAdapter;
import com.example.fyp.models.AdminLog;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.jspecify.annotations.NonNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminLogsActivity extends AppCompatActivity {

    private Button btnStartDate, btnEndDate, btnResetFilters;
    private SearchView searchView;
    private RecyclerView recyclerView;

    private List<AdminLog> logs = new ArrayList<>();
    private List<AdminLog> filteredLogs = new ArrayList<>();
    private AdminLogAdapter adapter;

    private Date startDate = null;
    private Date endDate = null;

    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private static final int PAGE_SIZE = 20;
    private DocumentSnapshot lastVisible = null;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private TextView lastUpdatedTextView;
    private final SimpleDateFormat lastUpdatedFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    private LinearLayout errorLayout;
    private TextView errorMessage;
    private Button btnRetry;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_logs);

        btnStartDate = findViewById(R.id.btnStartDate);
        btnEndDate = findViewById(R.id.btnEndDate);
        searchView = findViewById(R.id.searchView);
        recyclerView = findViewById(R.id.recyclerViewLogs);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminLogAdapter(filteredLogs, this::onUndoClicked);
        recyclerView.setAdapter(adapter);

        btnStartDate.setOnClickListener(v -> showDatePicker(true));
        btnEndDate.setOnClickListener(v -> showDatePicker(false));

        btnResetFilters.setOnClickListener(v -> resetFilters());

        loadLogs();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterLogs(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterLogs(newText);
                return true;
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager == null) return;

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= PAGE_SIZE) {
                        loadLogs();
                    }

                }
            }
        });

        btnRetry.setOnClickListener(v -> {
            errorLayout.setVisibility(View.GONE);
            loadLogs();
        });

    }

    private void resetFilters() {
        startDate = null;
        endDate = null;
        btnStartDate.setText("Start Date");
        btnEndDate.setText("End Date");
        searchView.setQuery("", false);
        searchView.clearFocus();

        lastVisible = null;
        isLastPage = false;
        logs.clear();
        filteredLogs.clear();

        adapter.setSearchQuery("");
        adapter.notifyDataSetChanged();

        loadLogs();
    }
    private void showDatePicker(boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();
        Date initialDate = isStartDate ? startDate : endDate;
        if (initialDate != null) {
            calendar.setTime(initialDate);
        }

        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    Calendar pickedDate = Calendar.getInstance();
                    pickedDate.set(year, month, dayOfMonth);
                    if (isStartDate) {
                        startDate = pickedDate.getTime();
                        btnStartDate.setText("Start: " + displayDateFormat.format(startDate));
                    } else {
                        endDate = pickedDate.getTime();
                        btnEndDate.setText("End: " + displayDateFormat.format(endDate));
                    }
                    filterLogs(searchView.getQuery().toString());
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void loadLogs() {
        if (isLoading || isLastPage) return;
        isLoading = true;

        errorLayout.setVisibility(View.GONE);

        adapter.addLoadingFooter();

    FirebaseFirestore db = null;
        Query query = db.collection("adminLogs")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(PAGE_SIZE);

        if (lastVisible != null) {
            query = query.startAfter(lastVisible);
        }
        query.get().addOnSuccessListener(querySnapshot -> {
            adapter.removeLoadingFooter();
            isLoading = false;

            if (querySnapshot.size() < PAGE_SIZE) {
                isLastPage = true;
            }
            if (!querySnapshot.isEmpty()) {
                lastVisible = querySnapshot.getDocuments().get(querySnapshot.size() - 1);
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    AdminLog log = doc.toObject(AdminLog.class);
                    logs.add(log);
                }
                applyFiltersAndUpdate();
            }

            // Update last updated text
            String now = lastUpdatedFormat.format(new Date());
            lastUpdatedTextView.setText("Last updated: " + now);

        }).addOnFailureListener(e -> {
            adapter.removeLoadingFooter();
            isLoading = false;
            if (logs.isEmpty()) {
                errorMessage.setText("Failed to load logs: " + e.getMessage());
                errorLayout.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, "Failed to load logs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void applyFiltersAndUpdate() {
        filteredLogs.clear();
        String lowerQuery = (searchView.getQuery() == null) ? "" : searchView.getQuery().toString().toLowerCase();

        for (AdminLog log : logs) {
            boolean matchesQuery =
                    (log.getUserId() != null && log.getUserId().toLowerCase().contains(lowerQuery)) ||
                            (log.getChangedBy() != null && log.getChangedBy().toLowerCase().contains(lowerQuery)) ||
                            (log.getNewRole() != null && log.getNewRole().toLowerCase().contains(lowerQuery));

            boolean matchesStartDate = (startDate == null) ||
                    (log.getTimestamp() != null && !log.getTimestamp().before(startDate));

            boolean matchesEndDate = (endDate == null) ||
                    (log.getTimestamp() != null && !log.getTimestamp().after(endDate));

            if (matchesQuery && matchesStartDate && matchesEndDate) {
                filteredLogs.add(log);
            }
        }
        adapter.setSearchQuery(searchView.getQuery().toString());
        adapter.notifyDataSetChanged();
    }

    private void filterLogs(String query) {
        filteredLogs.clear();
        String lowerQuery = (query == null) ? "" : query.toLowerCase();

        for (AdminLog log : logs) {
            boolean matchesQuery =
                    (log.getUserId() != null && log.getUserId().toLowerCase().contains(lowerQuery)) ||
                            (log.getChangedBy() != null && log.getChangedBy().toLowerCase().contains(lowerQuery)) ||
                            (log.getNewRole() != null && log.getNewRole().toLowerCase().contains(lowerQuery));

            boolean matchesStartDate = (startDate == null) ||
                    (log.getTimestamp() != null && !log.getTimestamp().before(startDate));

            boolean matchesEndDate = (endDate == null) ||
                    (log.getTimestamp() != null && !log.getTimestamp().after(endDate));

            if (matchesQuery && matchesStartDate && matchesEndDate) {
                filteredLogs.add(log);
            }
        }
        adapter.setSearchQuery(query);
        adapter.notifyDataSetChanged();
    }

    private void onUndoClicked(AdminLog log) {
        // Undo logic here (same as before)
    }
}