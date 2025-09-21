package com.example.fyp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
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
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SustainabilityTipsActivity extends AppCompatActivity {

    private TipsAdapter adapter;
    private List<Tip> tipsList = new ArrayList<>();
    private List<Tip> filteredList = new ArrayList<>();
    private String selectedCategory = "Suggested"; // Default to Suggested
    private int tipsCompleted = 0;
    private int streakDays = 0; // Track consecutive days
    private double co2Saved = 0.0; // Track CO2 savings

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sustainability_tips);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize UI elements
        RecyclerView recyclerView = findViewById(R.id.recycler_tips);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        adapter = new TipsAdapter(filteredList, this::onTipCompleted);
        recyclerView.setAdapter(adapter);

        // Animate loading progress
        findViewById(R.id.progress_loading).setVisibility(View.VISIBLE);
        findViewById(R.id.progress_loading).startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));

        // Load and filter tips
        loadStaticTips();
        filterData("", selectedCategory);

        // Hide loading and animate content reveal
        findViewById(R.id.progress_loading).setVisibility(View.GONE);
        findViewById(R.id.main_content).setAlpha(0f);
        findViewById(R.id.main_content).animate().alpha(1f).setDuration(600).start();

        // Search functionality with debounce
        TextInputEditText searchTips = findViewById(R.id.search_tips);
        searchTips.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterData(s.toString(), selectedCategory);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Category Chips with animation
        ChipGroup chipGroup = findViewById(R.id.chip_group);
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            chip.setOnClickListener(v -> {
                v.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
                selectedCategory = chip.getText().toString();
                filterData(searchTips.getText().toString(), selectedCategory);
            });
        }

        // Update initial UI
        updateCompletionUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu); // Assume toolbar_menu.xml exists
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Handle back navigation
            return true;
        } else if (item.getItemId() == R.id.action_share) {
            shareProgress();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadStaticTips() {
        // Waste Reduction Tips
        tipsList.add(new Tip("tip1", "Avoid Plastics", "Avoid Single-Use Plastics", "Carry a reusable water bottle to reduce waste. (Saves ~0.5 kg CO₂/day)", "Waste Reduction", null, "Royal Belum"));
        tipsList.add(new Tip("tip2", "Eco Bags", "Use Eco-Friendly Bags", "Bring a cloth bag for shopping instead of plastic ones. (Saves ~0.2 kg CO₂/day)", "Waste Reduction", null, "Gua Tempurung"));
        tipsList.add(new Tip("tip3", "Reduce Food", "Reduce Food Waste", "Pack only what you can eat to minimize leftovers. (Saves ~1 kg CO₂/day)", "Waste Reduction", null, "Kuala Sepetang"));
        tipsList.add(new Tip("tip4", "Recycle", "Recycle Properly", "Sort your waste into recycling bins provided at sites. (Saves ~0.3 kg CO₂/day)", "Waste Reduction", null, "Royal Belum"));
        tipsList.add(new Tip("tip5", "No Cutlery", "Avoid Disposable Cutlery", "Use reusable utensils during your travels. (Saves ~0.4 kg CO₂/day)", "Waste Reduction", null, "Gua Tempurung"));

        // Wildlife Care Tips
        tipsList.add(new Tip("tip6", "Respect Wildlife", "Respect Wildlife", "Keep a safe distance from animals to avoid disturbing them. (Indirect impact)", "Wildlife Care", null, "Royal Belum"));
        tipsList.add(new Tip("tip7", "No Feeding", "Do Not Feed Animals", "Avoid feeding wildlife to maintain their natural behavior. (Indirect impact)", "Wildlife Care", null, "Gua Tempurung"));
        tipsList.add(new Tip("tip8", "Stay on Paths", "Stay on Designated Paths", "Stick to trails to protect habitats and wildlife. (Indirect impact)", "Wildlife Care", null, "Kuala Sepetang"));
        tipsList.add(new Tip("tip9", "No Flash", "Avoid Flash Photography", "Use natural light to prevent startling animals. (Indirect impact)", "Wildlife Care", null, "Royal Belum"));
        tipsList.add(new Tip("tip10", "Pet Cleanup", "Clean Up After Pets", "Ensure pets don’t disturb local wildlife. (Indirect impact)", "Wildlife Care", null, "Gua Tempurung"));

        // Community Respect Tips
        tipsList.add(new Tip("tip11", "Support Vendors", "Support Local Vendors", "Buy from local vendors to boost the economy. (Indirect impact)", "Community Respect", null, "Royal Belum"));
        tipsList.add(new Tip("tip12", "Learn Customs", "Learn Local Customs", "Respect cultural practices and ask for permission when needed. (Indirect impact)", "Community Respect", null, "Gua Tempurung"));
        tipsList.add(new Tip("tip13", "Leave No Trace", "Leave No Trace", "Keep sites clean to respect the community’s environment. (Saves ~0.1 kg CO₂/day)", "Community Respect", null, "Kuala Sepetang"));
        tipsList.add(new Tip("tip14", "Engage Respectfully", "Engage Respectfully", "Interact with locals politely and avoid intrusive behavior. (Indirect impact)", "Community Respect", null, "Royal Belum"));
        tipsList.add(new Tip("tip15", "Contribute", "Contribute to Conservation", "Donate to local conservation efforts if possible. (Indirect impact)", "Community Respect", null, "Gua Tempurung"));

        // Additional Tips
        tipsList.add(new Tip("tip16", "Solar Power", "Use Solar Chargers", "Power devices with solar energy to reduce carbon footprint. (Saves ~0.7 kg CO₂/day)", "Waste Reduction", null, "Royal Belum"));
        tipsList.add(new Tip("tip17", "Protect Coral", "Protect Coral Reefs", "Avoid touching or standing on coral during water activities. (Indirect impact)", "Wildlife Care", null, "Gua Tempurung"));
        tipsList.add(new Tip("tip18", "Cleanups", "Participate in Cleanups", "Join community-led clean-up events at ecotourism sites. (Saves ~1.5 kg CO₂/day)", "Community Respect", null, "Kuala Sepetang"));
        tipsList.add(new Tip("tip19", "Conserve Water", "Conserve Water", "Take shorter showers to preserve local water resources. (Saves ~0.3 kg CO₂/day)", "Waste Reduction", null, "Royal Belum"));
        tipsList.add(new Tip("tip20", "Report Issues", "Report Illegal Activities", "Inform authorities about poaching or littering. (Indirect impact)", "Wildlife Care", null, "Gua Tempurung"));
    }

    private void filterData(String query, String category) {
        filteredList.clear();
        for (Tip tip : tipsList) {
            boolean matchesQuery = tip.getText().toLowerCase().contains(query.toLowerCase()) ||
                    tip.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    tip.getDescription().toLowerCase().contains(query.toLowerCase());
            boolean matchesCategory = category.equals("Suggested") ? isSuggestedTip(tip) : category.equals("All") || tip.getCategory().equalsIgnoreCase(category);

            if (matchesQuery && matchesCategory) {
                filteredList.add(tip);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private boolean isSuggestedTip(Tip tip) {
        // Logic for "Suggested" tips (e.g., high impact or location-specific as of current time)
        String currentTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
        return tip.getLocation().equals("Royal Belum") || // Prioritize Royal Belum for early morning
                tip.getDescription().contains("Saves") && tip.getDescription().contains("kg CO₂"); // High-impact tips
    }

    private void onTipCompleted(Tip tip) {
        tipsCompleted++;
        streakDays++; // Increment streak (simplified; persist with SharedPreferences in production)
        co2Saved += extractCo2Savings(tip.getDescription()); // Parse CO2 savings
        updateCompletionUI();
        Snackbar.make(findViewById(android.R.id.content), "Completed: " + tip.getTitle(), Snackbar.LENGTH_SHORT).show();
    }

    private double extractCo2Savings(String description) {
        // Simple parsing of CO2 savings (e.g., "Saves ~0.5 kg CO₂/day")
        if (description.contains("Saves") && description.contains("kg CO₂")) {
            String[] parts = description.split("Saves ~| kg CO₂");
            for (String part : parts) {
                try {
                    return Double.parseDouble(part.trim());
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }
        return 0.0; // Default for indirect impact tips
    }

    private void updateCompletionUI() {
        double completionPercentage = (tipsCompleted / (double) tipsList.size()) * 100;
        findViewById(R.id.progress_completion).post(() -> {
            ((com.google.android.material.progressindicator.CircularProgressIndicator) findViewById(R.id.progress_completion))
                    .setProgress((int) completionPercentage, true);
            ((TextView) findViewById(R.id.tv_completion)).setText(String.format("%.0f%% Complete", completionPercentage));
            ((TextView) findViewById(R.id.tv_streak)).setText("Streak: " + streakDays + " days");
            ((TextView) findViewById(R.id.tv_impact)).setText(String.format("Impact: %.1f kg CO₂ saved", co2Saved));
        });

        ImageView badge = findViewById(R.id.iv_badge);
        if (tipsCompleted >= 5 && badge.getVisibility() != View.VISIBLE) {
            badge.setVisibility(View.VISIBLE);
            badge.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
            Toast.makeText(this, "Congratulations! You've earned the Eco-Warrior Badge!", Toast.LENGTH_LONG).show();
        }
    }

    private void shareProgress() {
        String shareText = String.format("My Eco Progress:\n- Tips Completed: %d\n- Streak: %d days\n- CO₂ Saved: %.1f kg\n#EcoTravel #Sustainability",
                tipsCompleted, streakDays, co2Saved);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Share Your Progress"));
    }
}