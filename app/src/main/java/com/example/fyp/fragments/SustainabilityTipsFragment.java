package com.example.fyp.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp.R;
import com.example.fyp.activities.TipDetailActivity;
import com.example.fyp.adapters.TipsAdapter;
import com.example.fyp.models.Tip;
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

public class SustainabilityTipsFragment extends Fragment implements TipsAdapter.OnTipActionListener {

    private static final String PREFS_NAME = "EcoTrackPrefs";
    private static final String KEY_COMPLETED_TIPS = "completed_tips_ids";
    private static final String KEY_STREAK = "tips_streak";
    private static final String KEY_LAST_ACTIVITY_DATE = "last_activity_date";
    private static final String KEY_TOTAL_REDUCTION = "total_reduction";

    private RecyclerView recyclerTips;
    private TextInputEditText searchTips;
    private TextView tvNoResults, tvCompletion, tvStreak, tvImpact;
    private CircularProgressIndicator progressCompletion;
    private ImageView ivBadge;
    
    private final List<Tip> tipsList = new ArrayList<>();
    private final List<Tip> filteredList = new ArrayList<>();
    private TipsAdapter adapter;
    private String selectedCategory = "All";
    private int streakDays = 0;
    private double totalCo2Saved = 0.0;
    private Set<String> completedTipIds = new HashSet<>();
    private SharedPreferences prefs;
    private TextToSpeech tts;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getContext() != null) {
            prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            loadPersistenceData();
            checkAndResetStreak();
            
            tts = new TextToSpeech(getContext(), status -> {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(Locale.getDefault());
                    tts.setSpeechRate(0.9f);
                }
            });
        }
    }

    private void loadPersistenceData() {
        if (prefs != null) {
            Set<String> savedIds = prefs.getStringSet(KEY_COMPLETED_TIPS, new HashSet<>());
            completedTipIds = new HashSet<>(savedIds); // Defensive copy
            streakDays = prefs.getInt(KEY_STREAK, 0);
            totalCo2Saved = prefs.getFloat(KEY_TOTAL_REDUCTION, 0.0f);
        }
    }

    private void checkAndResetStreak() {
        if (prefs == null) return;
        String lastDate = prefs.getString(KEY_LAST_ACTIVITY_DATE, "");
        if (lastDate.isEmpty()) return;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Calendar today = Calendar.getInstance();
            Calendar last = Calendar.getInstance();
            last.setTime(sdf.parse(lastDate));

            // Set to midnight for accurate day difference
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            
            last.set(Calendar.HOUR_OF_DAY, 0);
            last.set(Calendar.MINUTE, 0);
            last.set(Calendar.SECOND, 0);
            last.set(Calendar.MILLISECOND, 0);

            long diff = today.getTimeInMillis() - last.getTimeInMillis();
            long days = diff / (24 * 60 * 60 * 1000);

            if (days > 1) { // More than 1 day since last activity
                streakDays = 0;
                prefs.edit().putInt(KEY_STREAK, 0).apply();
            }
        } catch (Exception ignored) {}
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tips, container, false);

        initViews(view);
        setupRecyclerView();
        setupSearchAndFilters(view);

        loadStaticTips();
        filter("");
        updateCompletionUI();

        return view;
    }

    private void initViews(View view) {
        searchTips = view.findViewById(R.id.search_tips);
        tvNoResults = view.findViewById(R.id.tv_no_results);
        recyclerTips = view.findViewById(R.id.recycler_tips);
        tvCompletion = view.findViewById(R.id.tv_completion);
        tvStreak = view.findViewById(R.id.tv_streak);
        tvImpact = view.findViewById(R.id.tv_impact);
        progressCompletion = view.findViewById(R.id.progress_completion);
        ivBadge = view.findViewById(R.id.iv_badge);

        if (ivBadge != null) {
            ivBadge.setOnClickListener(v -> 
                Toast.makeText(getContext(), "Eco-Warrior Badge: Awarded for completing 5+ sustainable actions!", Toast.LENGTH_LONG).show()
            );
        }
    }

    private void setupRecyclerView() {
        recyclerTips.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerTips.setHasFixedSize(true);
        adapter = new TipsAdapter(new ArrayList<>(filteredList), this);
        recyclerTips.setAdapter(adapter);
    }

    private void setupSearchAndFilters(View view) {
        searchTips.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        ChipGroup chipGroup = view.findViewById(R.id.chip_group);
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            chip.setOnClickListener(v -> {
                selectedCategory = chip.getText().toString();
                Editable text = searchTips.getText();
                filter(text != null ? text.toString() : "");
            });
        }
    }

    private void loadStaticTips() {
        tipsList.clear();
        addTip("tip1", "Avoid Plastics", "Avoid Single-Use Plastics", "Carry a reusable water bottle. (Saves ~0.5 kg CO₂)", "Waste Reduction", "royal_belum", "Royal Belum", 0.5);
        addTip("tip2", "Eco Bags", "Use Eco-Friendly Bags", "Bring a cloth bag for shopping. (Saves ~0.2 kg CO₂)", "Waste Reduction", "gua_tempurung", "Gua Tempurung", 0.2);
        addTip("tip3", "Reduce Food", "Reduce Food Waste", "Pack only what you can eat. (Saves ~1.0 kg CO₂)", "Waste Reduction", "kuala_sepetang", "Kuala Sepetang", 1.0);
        addTip("tip4", "Recycle", "Recycle Properly", "Sort your waste into recycling bins. (Saves ~0.3 kg CO₂)", "Waste Reduction", "royal_belum_1", "Royal Belum", 0.3);
        addTip("tip5", "No Cutlery", "Avoid Disposable Cutlery", "Use reusable utensils during your travels. (Saves ~0.4 kg CO₂)", "Waste Reduction", "gua_tempurung_1", "Gua Tempurung", 0.4);
        addTip("tip6", "Respect Wildlife", "Respect Wildlife", "Keep a safe distance from animals.", "Wildlife Care", "royal_belum_2", "Royal Belum", 0.0);
        addTip("tip7", "No Feeding", "Do Not Feed Animals", "Maintain their natural behavior.", "Wildlife Care", "gua_tempurung_2", "Gua Tempurung", 0.0);
        addTip("tip8", "Stay on Paths", "Stay on Designated Paths", "Stick to trails to protect habitats.", "Wildlife Care", "kuala_sepetang_1", "Kuala Sepetang", 0.0);
        addTip("tip9", "No Flash", "Avoid Flash Photography", "Use natural light to prevent startling animals.", "Wildlife Care", "royal_belum_3", "Royal Belum", 0.0);
        addTip("tip10", "Pet Cleanup", "Clean Up After Pets", "Ensure pets don’t disturb local wildlife.", "Wildlife Care", "gua_tempurung_3", "Gua Tempurung", 0.0);
        addTip("tip11", "Support Vendors", "Support Local Vendors", "Buy from local vendors to boost the economy.", "Community Respect", "royal_belum", "Royal Belum", 0.0);
        addTip("tip12", "Learn Customs", "Learn Local Customs", "Respect cultural practices.", "Community Respect", "gua_tempurung", "Gua Tempurung", 0.0);
        addTip("tip13", "Leave No Trace", "Leave No Trace", "Keep sites clean. (Saves ~0.1 kg CO₂)", "Community Respect", "kuala_sepetang", "Kuala Sepetang", 0.1);
    }

    private void addTip(String id, String title, String text, String desc, String cat, String img, String site, double co2) {
        Tip tip = new Tip(id, title, text, desc, cat, img, site, co2);
        if (completedTipIds.contains(id)) {
            tip.setCompleted(true);
        }
        tipsList.add(tip);
    }

    private void filter(String text) {
        filteredList.clear();
        String lowerQuery = text.toLowerCase();
        for (Tip tip : tipsList) {
            boolean matchesQuery = tip.getTitle().toLowerCase().contains(lowerQuery) ||
                    tip.getDescription().toLowerCase().contains(lowerQuery);
            
            boolean matchesCategory = selectedCategory.equals("All") || 
                    (selectedCategory.equals("Suggested") && isSuggested(tip)) || 
                    tip.getCategory().equalsIgnoreCase(selectedCategory);

            if (matchesQuery && matchesCategory) {
                filteredList.add(tip);
            }
        }
        tvNoResults.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
        adapter.updateTips(new ArrayList<>(filteredList));
    }

    private boolean isSuggested(Tip tip) {
        return tip.getSite().equals("Royal Belum") || 
               tip.getCo2Savings() > 0 ||
               tip.getCategory().equals("Wildlife Care");
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
            
            if (getView() != null) {
                Snackbar.make(getView(), "Tip completed: " + tip.getTitle(), Snackbar.LENGTH_SHORT).show();
            }
            
            adapter.updateTips(new ArrayList<>(filteredList));
        } else {
            Toast.makeText(getContext(), "You've already completed this tip!", Toast.LENGTH_SHORT).show();
        }

        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), TipDetailActivity.class);
            intent.putExtra("tip", tip);
            startActivity(intent);
        }
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
        if (prefs != null) {
            prefs.edit()
                .putStringSet(KEY_COMPLETED_TIPS, completedTipIds)
                .putInt(KEY_STREAK, streakDays)
                .putFloat(KEY_TOTAL_REDUCTION, (float) totalCo2Saved)
                .apply();
        }
    }

    @Override
    public void onSpeakTip(Tip tip) {
        if (tts != null) {
            String textToSpeak = tip.getTitle() + ". " + tip.getDescription();
            tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "TipSpeechFragment");
        }
    }

    private void updateCompletionUI() {
        int totalTipsCount = tipsList.size();
        if (totalTipsCount == 0) return;

        int completedCount = completedTipIds.size();
        double percentage = (completedCount / (double) totalTipsCount) * 100;
        
        if (progressCompletion != null) progressCompletion.setProgress((int) percentage, true);
        if (tvCompletion != null) tvCompletion.setText(String.format(Locale.getDefault(), "%.0f%% Complete", percentage));
        if (tvStreak != null) tvStreak.setText(String.format(Locale.getDefault(), "Streak: %d days", streakDays));
        if (tvImpact != null) tvImpact.setText(String.format(Locale.getDefault(), "Impact: %.1f kg CO₂ saved", totalCo2Saved));

        if (completedCount >= 5 && ivBadge != null && ivBadge.getVisibility() != View.VISIBLE) {
            ivBadge.setVisibility(View.VISIBLE);
            ivBadge.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));
            Toast.makeText(getContext(), "Earned the Eco-Warrior Badge!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
