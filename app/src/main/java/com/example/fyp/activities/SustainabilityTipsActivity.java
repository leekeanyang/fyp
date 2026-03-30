package com.example.fyp.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp.R;
import com.example.fyp.adapters.TipsAdapter;
import com.example.fyp.models.Tip;
import com.example.fyp.repositories.SiteCatalog;
import com.example.fyp.repositories.TipRepository;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class SustainabilityTipsActivity extends AppCompatActivity implements TipsAdapter.OnTipActionListener {

    private static final String PREFS_NAME = "EcoTrackPrefs";
    private static final String TRIP_PREFS = "TripPlannerPrefs";
    private static final String KEY_COMPLETED_TIPS = "completed_tips_ids";
    private static final String KEY_STREAK = "tips_streak";
    private static final String KEY_LAST_ACTIVITY_DATE = "last_activity_date";
    private static final String KEY_TOTAL_REDUCTION = "total_reduction";
    private static final String KEY_SELECTED_SITE = "selected_site_name";

    private TipsAdapter adapter;
    private final List<Tip> tipsList = new ArrayList<>();
    private final List<Tip> filteredList = new ArrayList<>();
    private String selectedCategory = "All";
    private String selectedSite = SiteCatalog.ALL_SITES;
    private int streakDays = 0;
    private double totalCo2Saved = 0.0;
    private Set<String> completedTipIds = new HashSet<>();
    private SharedPreferences prefs;
    private TextToSpeech tts;

    private CircularProgressIndicator progressCompletion;
    private TextView tvCompletion;
    private TextView tvStreak;
    private TextView tvImpact;
    private TextView tipsHeader;
    private ImageView ivBadge;
    private View mainContent;
    private ProgressBar progressLoading;
    private TextInputEditText searchTips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sustainability_tips);

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadPersistenceData();
        checkAndResetStreak();

        initViews();
        setupToolbar();
        setupTTS();
        setupRecyclerView();
        setupSearchAndFilters();

        selectedSite = resolveSelectedSite();
        loadTips();
        filterData("", selectedCategory);
        updateHeader();

        revealContent();
        updateCompletionUI();
    }

    private void loadPersistenceData() {
        Set<String> savedIds = prefs.getStringSet(KEY_COMPLETED_TIPS, new HashSet<>());
        completedTipIds = new HashSet<>(savedIds);
        streakDays = prefs.getInt(KEY_STREAK, 0);
        totalCo2Saved = prefs.getFloat(KEY_TOTAL_REDUCTION, 0.0f);
    }

    private void checkAndResetStreak() {
        String lastDate = prefs.getString(KEY_LAST_ACTIVITY_DATE, "");
        if (lastDate.isEmpty()) {
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Calendar today = Calendar.getInstance();
            Calendar last = Calendar.getInstance();
            last.setTime(sdf.parse(lastDate));

            resetCalendarTime(today);
            resetCalendarTime(last);

            long diff = today.getTimeInMillis() - last.getTimeInMillis();
            long days = diff / (24L * 60L * 60L * 1000L);
            if (days > 1) {
                streakDays = 0;
                prefs.edit().putInt(KEY_STREAK, 0).apply();
            }
        } catch (Exception ignored) {
        }
    }

    private void resetCalendarTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private void initViews() {
        progressCompletion = findViewById(R.id.progress_completion);
        tvCompletion = findViewById(R.id.tv_completion);
        tvStreak = findViewById(R.id.tv_streak);
        tvImpact = findViewById(R.id.tv_impact);
        tipsHeader = findViewById(R.id.tips_header);
        ivBadge = findViewById(R.id.iv_badge);
        mainContent = findViewById(R.id.main_content);
        progressLoading = findViewById(R.id.progress_loading);
        searchTips = findViewById(R.id.search_tips);

        ivBadge.setOnClickListener(v ->
                Toast.makeText(this, "Eco-Warrior badge unlocked after five completed actions.", Toast.LENGTH_SHORT).show()
        );
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Eco-Friendly Travel Tips");
        }
    }

    private void setupTTS() {
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.getDefault());
                tts.setSpeechRate(0.9f);
            }
        });
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_tips);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        adapter = new TipsAdapter(new ArrayList<>(filteredList), this);
        recyclerView.setAdapter(adapter);
    }

    private void setupSearchAndFilters() {
        searchTips.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterData(s.toString(), selectedCategory);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        ChipGroup chipGroup = findViewById(R.id.chip_group);
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            chip.setOnClickListener(v -> {
                selectedCategory = chip.getText().toString();
                Editable text = searchTips.getText();
                filterData(text != null ? text.toString() : "", selectedCategory);
            });
        }
    }

    private String resolveSelectedSite() {
        String siteName = getIntent().getStringExtra("siteName");
        if (siteName != null && !siteName.trim().isEmpty()) {
            return siteName;
        }

        SharedPreferences tripPrefs = getSharedPreferences(TRIP_PREFS, MODE_PRIVATE);
        String rememberedSite = tripPrefs.getString(KEY_SELECTED_SITE, SiteCatalog.ALL_SITES);
        return rememberedSite != null ? rememberedSite : SiteCatalog.ALL_SITES;
    }

    private void loadTips() {
        tipsList.clear();
        List<Tip> tips = TipRepository.getStaticTips();
        for (Tip tip : tips) {
            tip.setCompleted(completedTipIds.contains(tip.getId()));
            tipsList.add(tip);
        }
    }

    private void updateHeader() {
        if (tipsHeader == null) {
            return;
        }

        if (SiteCatalog.ALL_SITES.equals(selectedSite)) {
            tipsHeader.setText("Perak Sustainability Tips");
        } else {
            tipsHeader.setText("Tips for " + selectedSite);
            Toast.makeText(this, "Showing site-specific tips for " + selectedSite, Toast.LENGTH_SHORT).show();
        }
    }

    private void revealContent() {
        if (progressLoading != null) {
            progressLoading.setVisibility(View.GONE);
        }
        if (mainContent != null) {
            mainContent.setAlpha(0f);
            mainContent.setVisibility(View.VISIBLE);
            mainContent.animate().alpha(1f).setDuration(400).start();
        }
    }

    @Override
    public void onTipCompleted(Tip tip) {
        if (!tip.isCompleted()) {
            tip.setCompleted(true);
            completedTipIds.add(tip.getId());
            updateStreak();
            totalCo2Saved += tip.getCo2Savings();
            savePersistenceData();
            updateCompletionUI();

            Snackbar.make(mainContent != null ? mainContent : findViewById(android.R.id.content),
                    "Tip completed: " + tip.getTitle(), Snackbar.LENGTH_SHORT).show();
            adapter.updateTips(new ArrayList<>(filteredList));
        } else {
            Toast.makeText(this, "Already completed.", Toast.LENGTH_SHORT).show();
        }

        Intent intent = new Intent(this, TipDetailActivity.class);
        intent.putExtra("tip", tip);
        startActivity(intent);
    }

    private void updateStreak() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayDate = sdf.format(Calendar.getInstance().getTime());
        String lastDate = prefs.getString(KEY_LAST_ACTIVITY_DATE, "");

        if (!todayDate.equals(lastDate)) {
            streakDays++;
            prefs.edit().putString(KEY_LAST_ACTIVITY_DATE, todayDate).apply();
        }
    }

    private void savePersistenceData() {
        prefs.edit()
                .putStringSet(KEY_COMPLETED_TIPS, completedTipIds)
                .putInt(KEY_STREAK, streakDays)
                .putFloat(KEY_TOTAL_REDUCTION, (float) totalCo2Saved)
                .apply();
    }

    @Override
    public void onSpeakTip(Tip tip) {
        if (tts != null) {
            String textToSpeak = tip.getTitle() + ". " + tip.getDescription();
            tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "TipSpeech");
        }
    }

    private void updateCompletionUI() {
        if (tipsList.isEmpty()) {
            return;
        }

        int completedCount = completedTipIds.size();
        double percentage = (completedCount / (double) tipsList.size()) * 100.0;

        progressCompletion.setProgress((int) percentage, true);
        tvCompletion.setText(String.format(Locale.getDefault(), "%.0f%% Complete", percentage));
        tvStreak.setText(String.format(Locale.getDefault(), "Streak: %d days", streakDays));
        tvImpact.setText(String.format(Locale.getDefault(), "Impact: %.1f kg CO2 saved", totalCo2Saved));

        if (completedCount >= 5 && ivBadge.getVisibility() != View.VISIBLE) {
            ivBadge.setVisibility(View.VISIBLE);
            ivBadge.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
            Toast.makeText(this, "Eco-Warrior badge unlocked.", Toast.LENGTH_LONG).show();
        }
    }

    private void filterData(String query, String category) {
        filteredList.clear();
        String lowerQuery = query.toLowerCase(Locale.getDefault());

        for (Tip tip : tipsList) {
            boolean matchesQuery = tip.getTitle().toLowerCase(Locale.getDefault()).contains(lowerQuery)
                    || tip.getDescription().toLowerCase(Locale.getDefault()).contains(lowerQuery);

            boolean matchesCategory = "All".equals(category)
                    || ("Suggested".equals(category) && isSuggested(tip))
                    || tip.getCategory().equalsIgnoreCase(category);

            boolean matchesSite = SiteCatalog.ALL_SITES.equals(selectedSite)
                    || tip.getSite().equalsIgnoreCase(selectedSite);

            if (matchesQuery && matchesCategory && matchesSite) {
                filteredList.add(tip);
            }
        }

        adapter.updateTips(new ArrayList<>(filteredList));
    }

    private boolean isSuggested(Tip tip) {
        if (!SiteCatalog.ALL_SITES.equals(selectedSite) && tip.getSite().equalsIgnoreCase(selectedSite)) {
            return true;
        }
        return tip.getCo2Savings() > 0 || "Wildlife Care".equalsIgnoreCase(tip.getCategory());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_share) {
            shareProgress();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareProgress() {
        String shareText = String.format(Locale.getDefault(),
                "My eco progress in Perak:\nTips completed: %d\nStreak: %d days\nEstimated CO2 saved: %.1f kg",
                completedTipIds.size(), streakDays, totalCo2Saved);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Share Progress"));
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
