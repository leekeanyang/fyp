package com.example.fyp.repositories;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.ai.FirebaseAI;
import com.google.firebase.ai.GenerativeModel;
import com.google.firebase.ai.java.GenerativeModelFutures;
import com.google.firebase.ai.type.Content;
import com.google.firebase.ai.type.GenerateContentResponse;
import com.google.firebase.ai.type.GenerativeBackend;

import java.util.Locale;

public class AiItineraryPlanner {

    public interface PlannerCallback {
        void onPlanReady(String planText);
    }

    public void buildPlan(Context context,
                          SiteCatalog.SiteMeta meta,
                          String interest,
                          String availableTime,
                          String season,
                          int visitorCount,
                          PlannerCallback callback) {
        String fallbackPlan = SiteCatalog.buildFallbackPlan(meta, interest, availableTime, season, visitorCount);

        try {
            GenerativeModel model = FirebaseAI.getInstance(GenerativeBackend.googleAI())
                    .generativeModel("gemini-2.5-flash");
            GenerativeModelFutures futures = GenerativeModelFutures.from(model);
            Content prompt = new Content.Builder()
                    .addText(buildPrompt(meta, interest, availableTime, season, visitorCount))
                    .build();
            ListenableFuture<GenerateContentResponse> future = futures.generateContent(prompt);

            Futures.addCallback(future, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    String text = result != null ? result.getText() : null;
                    if (text == null || text.trim().isEmpty()) {
                        callback.onPlanReady(fallbackPlan);
                        return;
                    }
                    callback.onPlanReady(text.trim());
                }

                @Override
                public void onFailure(Throwable throwable) {
                    callback.onPlanReady(fallbackPlan);
                }
            }, ContextCompat.getMainExecutor(context));
        } catch (Throwable ignored) {
            callback.onPlanReady(fallbackPlan);
        }
    }

    private String buildPrompt(SiteCatalog.SiteMeta meta,
                               String interest,
                               String availableTime,
                               String season,
                               int visitorCount) {
        return "You are planning a sustainable visit for an ecotourism mobile app in Perak, Malaysia.\n"
                + "Site: " + meta.getName() + "\n"
                + "Visitor interest: " + safeValue(interest) + "\n"
                + "Available time: " + safeValue(availableTime) + "\n"
                + "Season: " + safeValue(season) + "\n"
                + "Current visitors: " + visitorCount + " / " + meta.getMaxCapacity() + "\n"
                + "Site strengths: " + meta.getBestFor() + "\n"
                + "Season note: " + meta.getSeasonalAdvice() + "\n"
                + "Sustainability reminder: " + meta.getEcoTrackAdvice() + "\n\n"
                + "Write one short itinerary suggestion in plain English with:\n"
                + "1. A recommended visit time window.\n"
                + "2. One route or activity order.\n"
                + "3. One crowd-management note.\n"
                + "4. One eco-friendly transport or behavior tip.\n"
                + "Keep it under 90 words and suitable for a mobile app card.";
    }

    private String safeValue(String value) {
        return value == null || value.trim().isEmpty() ? "General sightseeing" : value.trim();
    }
}
