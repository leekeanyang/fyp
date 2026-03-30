package com.example.fyp.activities;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.fyp.R;
import com.example.fyp.models.CarbonFootprint;
import com.example.fyp.repositories.SiteCatalog;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EcoTrackActivity extends AppCompatActivity {

    private static final int RECORD_AUDIO_PERMISSION = 1002;
    private static final String PREFS_NAME = "EcoTrackPrefs";
    private static final String KEY_SAVED_CALC = "saved_calc_";
    private static final String KEY_SAVED_CO2 = "saved_co2_";
    private static final String KEY_SAVED_COUNT = "saved_count";
    private static final String KEY_TOTAL_REDUCTION = "total_reduction";
    private static final String TRIP_PREFS = "TripPlannerPrefs";

    private TextInputLayout distanceLayout, nightsLayout;
    private TextInputEditText distance, nights;
    private Spinner transportMode, accommodationType, mealPreference;
    private TextView result, ecoTips, toggleTips, reductionLabel, plannerSummary;
    private MaterialButton calculate, reset, toggleHistory;
    private MaterialCardView resultCard;
    private CircularProgressIndicator loadingIndicator;
    private LinearProgressIndicator transportBar, accommodationBar, mealBar, reductionProgress;
    private ImageView iconTransport, iconAccommodation, iconMeals, badgeIcon;
    private TextView labelTransport, labelAccommodation, labelMeals;
    private FloatingActionButton shareButton, saveButton;
    private MaterialButton speakTransport, speakAccommodation, speakMeal;
    private SharedPreferences prefs;
    private ActivityResultLauncher<Intent> voiceLauncher;
    private boolean showingHistory = false, showingTips = false, showingContributions = true;
    private double totalReduction = 0.0;
    private String selectedSiteName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eco_track);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        totalReduction = prefs.getFloat(KEY_TOTAL_REDUCTION, 0.0f);

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Inputs and UI elements
        distanceLayout = findViewById(R.id.distance_layout);
        distance = findViewById(R.id.distance);
        nightsLayout = findViewById(R.id.nights_layout);
        nights = findViewById(R.id.nights);
        transportMode = findViewById(R.id.transport_mode);
        accommodationType = findViewById(R.id.accommodation_type);
        mealPreference = findViewById(R.id.meal_preference);

        calculate = findViewById(R.id.calculate);
        reset = findViewById(R.id.reset);
        toggleHistory = findViewById(R.id.toggle_history);
        result = findViewById(R.id.result);
        ecoTips = findViewById(R.id.eco_tips);
        toggleTips = findViewById(R.id.toggle_tips);
        resultCard = findViewById(R.id.result_card);
        loadingIndicator = findViewById(R.id.loading_indicator);
        reductionProgress = findViewById(R.id.reduction_progress);
        reductionLabel = findViewById(R.id.reduction_label);
        plannerSummary = findViewById(R.id.planner_summary);

        // Progress bars and icons
        transportBar = findViewById(R.id.progress_transport);
        accommodationBar = findViewById(R.id.progress_accommodation);
        mealBar = findViewById(R.id.progress_meals);
        iconTransport = findViewById(R.id.icon_transport);
        iconAccommodation = findViewById(R.id.icon_accommodation);
        iconMeals = findViewById(R.id.icon_meals);
        labelTransport = findViewById(R.id.label_transport);
        labelAccommodation = findViewById(R.id.label_accommodation);
        labelMeals = findViewById(R.id.label_meals);
        badgeIcon = findViewById(R.id.badge_icon);

        // Buttons
        shareButton = findViewById(R.id.fab_share);
        saveButton = findViewById(R.id.fab_save);
        speakTransport = findViewById(R.id.speak_transport);
        speakAccommodation = findViewById(R.id.speak_accommodation);
        speakMeal = findViewById(R.id.speak_meal);

        // Initial state
        resultCard.setAlpha(0f);
        shareButton.setVisibility(View.GONE);
        saveButton.setVisibility(View.GONE);
        toggleHistory.setVisibility(View.GONE);
        toggleTips.setVisibility(View.GONE);
        updateReductionProgress();
        loadPlannerContext();

        // Button listeners
        calculate.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            calculateCarbonFootprint();
        });
        reset.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            resetInputs();
        });
        shareButton.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            shareResults();
        });
        saveButton.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            saveCalculation();
        });
        toggleHistory.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            toggleHistoryView();
        });
        toggleTips.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            toggleEcoTips(v);
        });

        // Voice Launcher
        voiceLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                ArrayList<String> matches = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (matches != null && !matches.isEmpty()) {
                    String command = matches.get(0).toLowerCase();
                    processVoiceCommand(command);
                } else {
                    Toast.makeText(this, R.string.voice_error, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Real-time validation
        TextWatcher validator = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateInput(s, distanceLayout, "Must be positive");
                validateInput(s, nightsLayout, "Must be non-negative");
            }
            @Override public void afterTextChanged(Editable s) {}
        };
        distance.addTextChangedListener(validator);
        nights.addTextChangedListener(validator);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.eco_track_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_load) {
            loadLastCalculation();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void calculateCarbonFootprint() {
        loadingIndicator.setVisibility(View.VISIBLE);
        new Handler().postDelayed(() -> {
            String distanceStr = distance.getText().toString().trim();
            String nightsStr = nights.getText().toString().trim();

            if (TextUtils.isEmpty(distanceStr)) {
                distanceLayout.setError(getString(R.string.error_distance_required));
                loadingIndicator.setVisibility(View.GONE);
                return;
            }
            if (TextUtils.isEmpty(nightsStr)) {
                nightsLayout.setError(getString(R.string.error_nights_required));
                loadingIndicator.setVisibility(View.GONE);
                return;
            }
            if (transportMode.getSelectedItem() == null) {
                Toast.makeText(this, R.string.error_select_transport, Toast.LENGTH_SHORT).show();
                loadingIndicator.setVisibility(View.GONE);
                return;
            }
            if (accommodationType.getSelectedItem() == null) {
                Toast.makeText(this, R.string.error_select_accommodation, Toast.LENGTH_SHORT).show();
                loadingIndicator.setVisibility(View.GONE);
                return;
            }
            if (mealPreference.getSelectedItem() == null) {
                Toast.makeText(this, R.string.error_select_meal, Toast.LENGTH_SHORT).show();
                loadingIndicator.setVisibility(View.GONE);
                return;
            }

            try {
                double distanceVal = Double.parseDouble(distanceStr);
                int nightsVal = Integer.parseInt(nightsStr);

                if (distanceVal <= 0 || nightsVal < 0) {
                    Toast.makeText(this, R.string.error_positive_values, Toast.LENGTH_SHORT).show();
                    loadingIndicator.setVisibility(View.GONE);
                    return;
                }

                String transport = transportMode.getSelectedItem().toString();
                String accommodation = accommodationType.getSelectedItem().toString();
                String meal = mealPreference.getSelectedItem().toString();

                double transportCO2 = CarbonFootprint.getTransportFactor(transport) * distanceVal;
                double accommodationCO2 = CarbonFootprint.getAccommodationFactor(accommodation) * nightsVal;
                double mealCO2 = CarbonFootprint.getMealFactor(meal) * (nightsVal + 1);
                double totalCO2 = transportCO2 + accommodationCO2 + mealCO2;

                if (totalCO2 == 0) {
                    Toast.makeText(this, R.string.error_invalid_selections, Toast.LENGTH_SHORT).show();
                    loadingIndicator.setVisibility(View.GONE);
                    return;
                }

                int count = prefs.getInt(KEY_SAVED_COUNT, 0);
                double previousTotal = count > 0 ? prefs.getFloat(KEY_SAVED_CO2 + count, 0.0f) : 0.0;
                double reduction = previousTotal > 0 ? Math.max(0, previousTotal - totalCO2) : 0;
                totalReduction += reduction;
                prefs.edit().putFloat(KEY_TOTAL_REDUCTION, (float) totalReduction).apply();

                result.setText(String.format(Locale.getDefault(), "%s %.2f kg", getString(R.string.estimated_co2), totalCO2));
                ecoTips.setText(getEcoTip(transport, accommodation, meal, totalCO2));
                updateReductionProgress();

                resultCard.animate()
                        .alpha(1f)
                        .setDuration(500)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                loadingIndicator.setVisibility(View.GONE);
                                shareButton.setVisibility(View.VISIBLE);
                                saveButton.setVisibility(View.VISIBLE);
                                toggleHistory.setVisibility(View.VISIBLE);
                                toggleTips.setVisibility(View.VISIBLE);
                                animateButtons();
                            }
                        });

                new Handler().postDelayed(() -> highlightHighestContributor(transportCO2, accommodationCO2, mealCO2, totalCO2), 200);

                int color = totalCO2 < 50 ? R.color.eco_green : (totalCO2 < 200 ? R.color.eco_yellow : R.color.eco_red);
                result.setTextColor(ContextCompat.getColor(this, color));
                updateBadge(totalCO2);

            } catch (NumberFormatException e) {
                Toast.makeText(this, R.string.error_invalid_numbers, Toast.LENGTH_SHORT).show();
                loadingIndicator.setVisibility(View.GONE);
            }
        }, 1000);
    }

    private void loadPlannerContext() {
        SharedPreferences tripPrefs = getSharedPreferences(TRIP_PREFS, MODE_PRIVATE);
        selectedSiteName = getIntent().getStringExtra("siteName");
        if (selectedSiteName == null || selectedSiteName.trim().isEmpty()) {
            selectedSiteName = tripPrefs.getString("selected_site_name", "");
        }

        String itineraryPlan = getIntent().getStringExtra("itineraryPlan");
        if (itineraryPlan == null || itineraryPlan.trim().isEmpty()) {
            itineraryPlan = tripPrefs.getString("itinerary_plan", "");
        }

        float routeDistanceKm = getIntent().getFloatExtra("routeDistanceKm", -1f);
        if (routeDistanceKm < 0f) {
            routeDistanceKm = tripPrefs.getFloat("route_distance_km", -1f);
        }

        if (routeDistanceKm > 0f && distance.getText() != null && distance.getText().toString().trim().isEmpty()) {
            distance.setText(String.format(Locale.getDefault(), "%.1f", routeDistanceKm));
        }

        if (!selectedSiteName.isEmpty() || !itineraryPlan.isEmpty()) {
            String summary = selectedSiteName.isEmpty()
                    ? itineraryPlan
                    : "Linked itinerary for " + selectedSiteName + ":\n" + itineraryPlan;
            plannerSummary.setText(summary);
        }
    }

    private void highlightHighestContributor(double transportCO2, double accommodationCO2, double mealCO2, double totalCO2) {
        if (totalCO2 == 0) return;

        iconTransport.setVisibility(View.GONE);
        iconAccommodation.setVisibility(View.GONE);
        iconMeals.setVisibility(View.GONE);

        int maxProgress = 100;
        transportBar.setMax(maxProgress);
        accommodationBar.setMax(maxProgress);
        mealBar.setMax(maxProgress);

        int transportProgress = (int) ((transportCO2 / totalCO2) * maxProgress);
        int accommodationProgress = (int) ((accommodationCO2 / totalCO2) * maxProgress);
        int mealProgress = (int) ((mealCO2 / totalCO2) * maxProgress);

        transportBar.setProgress(transportProgress, true);
        accommodationBar.setProgress(accommodationProgress, true);
        mealBar.setProgress(mealProgress, true);

        double max = Math.max(transportCO2, Math.max(accommodationCO2, mealCO2));
        if (transportCO2 == max && transportCO2 > 0) animateIcon(iconTransport);
        if (accommodationCO2 == max && accommodationCO2 > 0) animateIcon(iconAccommodation);
        if (mealCO2 == max && mealCO2 > 0) animateIcon(iconMeals);

        labelTransport.setText(String.format(Locale.getDefault(), "%s: %.2f kg (%.0f%%)", getString(R.string.transport_label), transportCO2, (transportCO2 / totalCO2 * 100)));
        labelAccommodation.setText(String.format(Locale.getDefault(), "%s: %.2f kg (%.0f%%)", getString(R.string.accommodation_label), accommodationCO2, (accommodationCO2 / totalCO2 * 100)));
        labelMeals.setText(String.format(Locale.getDefault(), "%s: %.2f kg (%.0f%%)", getString(R.string.meals_label), mealCO2, (mealCO2 / totalCO2 * 100)));
    }

    private void updateBadge(double totalCO2) {
        if (totalCO2 < 50) {
            badgeIcon.setVisibility(View.VISIBLE);
            badgeIcon.setImageResource(R.drawable.ic_eco_badge_gold);
            badgeIcon.animate().scaleX(1f).scaleY(1f).setDuration(300).start();
        } else if (totalCO2 < 200) {
            badgeIcon.setVisibility(View.VISIBLE);
            badgeIcon.setImageResource(R.drawable.ic_eco_badge_silver);
            badgeIcon.animate().scaleX(1f).scaleY(1f).setDuration(300).start();
        } else {
            badgeIcon.setVisibility(View.GONE);
        }
    }

    private void animateButtons() {
        shareButton.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(300).start();
        saveButton.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(300).start();
        toggleHistory.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(300).start();
        toggleTips.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(300).start();
    }

    private void animateIcon(ImageView icon) {
        icon.setVisibility(View.VISIBLE);
        icon.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(500)
                .withEndAction(() -> icon.animate().scaleX(1f).scaleY(1f).setDuration(500).start())
                .start();
    }

    public void toggleEcoTips(View view) {
        showingTips = !showingTips;
        ecoTips.setMaxLines(showingTips ? Integer.MAX_VALUE : 3);
        toggleTips.setText(showingTips ? R.string.show_less : R.string.show_more);
    }

    public void toggleContributionSection(View view) {
        showingContributions = !showingContributions;
        findViewById(R.id.contribution_section).setVisibility(showingContributions ? View.VISIBLE : View.GONE);
    }

    private String getEcoTip(String transport, String accommodation, String meal, double totalCO2) {
        StringBuilder tips = new StringBuilder(getString(R.string.eco_tips_label) + " (Perak, " +
                new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date()) + "):\n");
        boolean hasTip = false;
        boolean isRainy = isRainyWeather();

        if (transport.contains("Car") || transport.contains("Flight")) {
            tips.append("- Use Perak’s buses or trains: ~80% less CO₂ than cars.\n");
            hasTip = true;
        }
        if (accommodation.equals("Hotel") || accommodation.equals("Resort")) {
            tips.append("- Try eco-lodges in Royal Belum: 5-10 kg CO₂/night vs. 20-25 kg.\n");
            hasTip = true;
        }
        if (meal.equals("Halal") || meal.equals("No Preference")) {
            tips.append("- Try veggie meals at Ipoh: Up to 60% less CO₂ than meat.\n");
            hasTip = true;
        }
        if (totalCO2 > 200) {
            tips.append("- High impact: Carpool or shorten trips in Perak.\n");
            hasTip = true;
        }
        if (isRainy) {
            tips.append("- Rainy at 03:19 AM: Rest indoors or plan for later.\n");
            hasTip = true;
        }
        if (!selectedSiteName.isEmpty()) {
            SiteCatalog.SiteMeta siteMeta = SiteCatalog.getSiteMetaByName(selectedSiteName);
            if (siteMeta != null) {
                tips.append("- ").append(siteMeta.getEcoTrackAdvice()).append("\n");
                hasTip = true;
            }
        }
        String challenge = getChallengeOfDay();
        if (!challenge.isEmpty()) {
            tips.append("- Challenge: ").append(challenge).append("\n");
            hasTip = true;
        }

        return hasTip ? tips.toString() : getString(R.string.tip_eco_friendly);
    }

    private String getChallengeOfDay() {
        Calendar cal = Calendar.getInstance();
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY && cal.get(Calendar.HOUR_OF_DAY) == 3) {
            return "Reduce 0.5 kg CO₂ today by choosing public transport!";
        }
        return "";
    }

    private boolean isRainyWeather() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.HOUR_OF_DAY) >= 0 && cal.get(Calendar.HOUR_OF_DAY) < 6 && cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY;
    }

    private void shareResults() {
        String shareText = result.getText().toString() + "\n\n" + ecoTips.getText().toString();
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_results)));
    }

    private void resetInputs() {
        distance.setText("");
        nights.setText("");
        transportMode.setSelection(0);
        accommodationType.setSelection(0);
        mealPreference.setSelection(0);
        resultCard.setAlpha(0f);
        shareButton.setVisibility(View.GONE);
        saveButton.setVisibility(View.GONE);
        toggleHistory.setVisibility(View.GONE);
        toggleTips.setVisibility(View.GONE);
        transportBar.setProgress(0, true);
        accommodationBar.setProgress(0, true);
        mealBar.setProgress(0, true);
        labelTransport.setText(R.string.transport_default);
        labelAccommodation.setText(R.string.accommodation_default);
        labelMeals.setText(R.string.meals_default);
        iconTransport.setVisibility(View.GONE);
        iconAccommodation.setVisibility(View.GONE);
        iconMeals.setVisibility(View.GONE);
        badgeIcon.setVisibility(View.GONE);
        distanceLayout.setError(null);
        nightsLayout.setError(null);
    }

    private void saveCalculation() {
        int count = prefs.getInt(KEY_SAVED_COUNT, 0) + 1;
        prefs.edit()
                .putFloat(KEY_SAVED_CO2 + count, (float) Double.parseDouble(result.getText().toString().replaceAll("[^\\d.]", "")))
                .putString(KEY_SAVED_CALC + count, result.getText().toString() + "\n" + ecoTips.getText().toString())
                .putInt(KEY_SAVED_COUNT, count)
                .apply();
        Toast.makeText(this, getString(R.string.calculation_saved, count), Toast.LENGTH_SHORT).show();
    }

    private void loadLastCalculation() {
        int count = prefs.getInt(KEY_SAVED_COUNT, 0);
        if (count > 0) {
            String lastCalc = prefs.getString(KEY_SAVED_CALC + count, "");
            if (!TextUtils.isEmpty(lastCalc)) {
                String[] parts = lastCalc.split("\n", 2);
                result.setText(parts[0]);
                ecoTips.setText(parts[1]);
                resultCard.setAlpha(1f);
                shareButton.setVisibility(View.VISIBLE);
                saveButton.setVisibility(View.VISIBLE);
                toggleHistory.setVisibility(View.VISIBLE);
                toggleTips.setVisibility(View.VISIBLE);
                animateButtons();
                Toast.makeText(this, R.string.calculation_loaded, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, R.string.no_saved_calculations, Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleHistoryView() {
        int count = prefs.getInt(KEY_SAVED_COUNT, 0);
        if (count > 0) {
            showingHistory = !showingHistory;
            if (showingHistory) {
                View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_history, null);
                TextView historyText = dialogView.findViewById(R.id.history_text);
                StringBuilder history = new StringBuilder();
                for (int i = 1; i <= count; i++) {
                    String calc = prefs.getString(KEY_SAVED_CALC + i, "");
                    if (!TextUtils.isEmpty(calc)) {
                        history.append(getString(R.string.entry, i)).append(": ").append(calc).append("\n\n");
                    }
                }
                historyText.setText(history.toString());
                new AlertDialog.Builder(this)
                        .setView(dialogView)
                        .setPositiveButton(R.string.close, (dialog, which) -> {
                            showingHistory = false;
                            toggleHistory.setText(R.string.view_history);
                            ecoTips.setText(getEcoTip(
                                    transportMode.getSelectedItem().toString(),
                                    accommodationType.getSelectedItem().toString(),
                                    mealPreference.getSelectedItem().toString(),
                                    Double.parseDouble(result.getText().toString().replaceAll("[^\\d.]", ""))));
                        })
                        .setOnDismissListener(dialog -> {
                            showingHistory = false;
                            toggleHistory.setText(R.string.view_history);
                        })
                        .show();
            } else {
                toggleHistory.setText(R.string.view_history);
            }
        } else {
            Toast.makeText(this, R.string.no_saved_calculations, Toast.LENGTH_SHORT).show();
        }
    }

    private void startVoiceInputForSpinner(Spinner spinner) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_PERMISSION);
            return;
        }
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_prompt_for_spinner, spinner.getContentDescription()));
        voiceLauncher.launch(intent);
    }

    private void processVoiceCommand(String command) {
        if (command.contains("distance")) {
            distance.setText(extractNumber(command));
        } else if (command.contains("nights")) {
            nights.setText(extractNumber(command));
        } else if (command.contains("transport")) {
            setSpinnerFromVoice(transportMode, command);
        } else if (command.contains("accommodation")) {
            setSpinnerFromVoice(accommodationType, command);
        } else if (command.contains("meal")) {
            setSpinnerFromVoice(mealPreference, command);
        } else {
            Toast.makeText(this, R.string.voice_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void setSpinnerFromVoice(Spinner spinner, String command) {
        String[] options = getResources().getStringArray(spinner.getId() == R.id.transport_mode ? R.array.transport_modes :
                spinner.getId() == R.id.accommodation_type ? R.array.accommodation_types : R.array.meal_preferences);
        String value = command.replaceAll("[^a-zA-Z\\s]", "").trim().toLowerCase();
        for (int i = 0; i < options.length; i++) {
            if (options[i].toLowerCase().contains(value)) {
                spinner.setSelection(i);
                return;
            }
        }
        Toast.makeText(this, getString(R.string.invalid_selection, spinner.getContentDescription()), Toast.LENGTH_SHORT).show();
    }

    private String extractNumber(String command) {
        String[] words = command.split("\\s+");
        for (String word : words) {
            if (word.matches("\\d+(\\.\\d+)?")) {
                return word;
            }
        }
        return "";
    }

    private void validateInput(CharSequence s, TextInputLayout layout, String errorMessage) {
        if (!TextUtils.isEmpty(s)) {
            try {
                double value = Double.parseDouble(s.toString());
                layout.setError(value <= 0 ? errorMessage : null);
            } catch (NumberFormatException e) {
                layout.setError(errorMessage);
            }
        } else {
            layout.setError(null);
        }
    }

    private void updateReductionProgress() {
        reductionProgress.setProgress((int) totalReduction, true);
        reductionLabel.setText(String.format(Locale.getDefault(), "%s: %.2f kg", getString(R.string.reduction_label_text), totalReduction));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RECORD_AUDIO_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // No action needed as voice input is now spinner-specific
        } else {
            Toast.makeText(this, R.string.audio_permission_denied, Toast.LENGTH_SHORT).show();
        }
    }
}
