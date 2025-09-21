package com.example.fyp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp.R;
import com.example.fyp.activities.TipDetailActivity;
import com.example.fyp.adapters.TipsAdapter;
import com.example.fyp.models.Tip;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class SustainabilityTipsFragment extends Fragment {

    private RecyclerView recyclerTips;
    private TextInputEditText searchTips;
    private TextView tvNoResults;
    private List<Tip> tipsList;
    private List<Tip> filteredList;
    private TipsAdapter adapter;
    private String selectedCategory = "All";
    private int tipsCompleted = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tips, container, false);

        searchTips = view.findViewById(R.id.search_tips);
        tvNoResults = view.findViewById(R.id.tv_no_results);
        recyclerTips = view.findViewById(R.id.recycler_tips);
        ChipGroup chipGroup = view.findViewById(R.id.chip_group);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerTips.setLayoutManager(layoutManager);
        recyclerTips.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));

        tipsList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new TipsAdapter(filteredList, this::onTipCompleted);
        recyclerTips.setAdapter(adapter);

        loadStaticTips();
        filter("");

        // Search functionality
        searchTips.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Category Chips
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            chip.setOnClickListener(v -> {
                selectedCategory = chip.getText().toString();
                filter(searchTips.getText().toString());
            });
        }

        return view;
    }

    private void loadStaticTips() {
        // Waste Reduction Tips
        tipsList.add(new Tip("tip1", "Avoid Plastics", "Avoid Single-Use Plastics", "Carry a reusable water bottle to reduce waste.", "Waste Reduction", "royal_belum", "Royal Belum"));
        tipsList.add(new Tip("tip2", "Eco Bags", "Use Eco-Friendly Bags", "Bring a cloth bag for shopping instead of plastic ones.", "Waste Reduction", "gua_tempurung", "Gua Tempurung"));
        tipsList.add(new Tip("tip3", "Reduce Food", "Reduce Food Waste", "Pack only what you can eat to minimize leftovers.", "Waste Reduction", "kuala_sepetang", "Kuala Sepetang"));
        tipsList.add(new Tip("tip4", "Recycle", "Recycle Properly", "Sort your waste into recycling bins provided at sites.", "Waste Reduction", "royal_belum_1", "Royal Belum"));
        tipsList.add(new Tip("tip5", "No Cutlery", "Avoid Disposable Cutlery", "Use reusable utensils during your travels.", "Waste Reduction", "gua_tempurung_1", "Gua Tempurung"));

        // Wildlife Care Tips
        tipsList.add(new Tip("tip6", "Respect Wildlife", "Respect Wildlife", "Keep a safe distance from animals to avoid disturbing them.", "Wildlife Care", "royal_belum_2", "Royal Belum"));
        tipsList.add(new Tip("tip7", "No Feeding", "Do Not Feed Animals", "Avoid feeding wildlife to maintain their natural behavior.", "Wildlife Care", "gua_tempurung_2", "Gua Tempurung"));
        tipsList.add(new Tip("tip8", "Stay on Paths", "Stay on Designated Paths", "Stick to trails to protect habitats and wildlife.", "Wildlife Care", "kuala_sepetang_1", "Kuala Sepetang"));
        tipsList.add(new Tip("tip9", "No Flash", "Avoid Flash Photography", "Use natural light to prevent startling animals.", "Wildlife Care", "royal_belum_3", "Royal Belum"));
        tipsList.add(new Tip("tip10", "Pet Cleanup", "Clean Up After Pets", "Ensure pets don’t disturb local wildlife.", "Wildlife Care", "gua_tempurung_3", "Gua Tempurung"));

        // Community Respect Tips
        tipsList.add(new Tip("tip11", "Support Vendors", "Support Local Vendors", "Buy from local vendors to boost the economy.", "Community Respect", "kuala_sepetang_2", "Kuala Sepetang"));
        tipsList.add(new Tip("tip12", "Learn Customs", "Learn Local Customs", "Respect cultural practices and ask for permission when needed.", "Community Respect", "royal_belum", "Royal Belum"));
        tipsList.add(new Tip("tip13", "Leave No Trace", "Leave No Trace", "Keep sites clean to respect the community’s environment.", "Community Respect", "gua_tempurung", "Gua Tempurung"));
        tipsList.add(new Tip("tip14", "Engage Respectfully", "Engage Respectfully", "Interact with locals politely and avoid intrusive behavior.", "Community Respect", "kuala_sepetang_3", "Kuala Sepetang"));
        tipsList.add(new Tip("tip15", "Contribute", "Contribute to Conservation", "Donate to local conservation efforts if possible.", "Community Respect", "royal_belum_1", "Royal Belum"));
    }

    private void filter(String text) {
        filteredList.clear();
        if (text.isEmpty() && selectedCategory.equals("All")) {
            filteredList.addAll(tipsList);
            tvNoResults.setVisibility(View.GONE);
        } else {
            for (Tip tip : tipsList) {
                boolean matchesQuery = tip.getTitle().toLowerCase().contains(text.toLowerCase()) ||
                        tip.getText().toLowerCase().contains(text.toLowerCase()) ||
                        tip.getDescription().toLowerCase().contains(text.toLowerCase());
                boolean matchesCategory = selectedCategory.equals("All") || tip.getCategory().equalsIgnoreCase(selectedCategory);

                if (matchesQuery && matchesCategory) {
                    filteredList.add(tip);
                }
            }
            tvNoResults.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
        }
        adapter.notifyDataSetChanged();
    }

    private void onTipCompleted(Tip tip) {
        tipsCompleted++;
        updateCompletionUI();
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), TipDetailActivity.class);
            intent.putExtra("tip", tip);
            startActivity(intent);
        }
    }

    private void updateCompletionUI() {
        if (getActivity() != null) {
            Toast.makeText(getActivity(), "Tips Completed: " + tipsCompleted, Toast.LENGTH_SHORT).show();
            ImageView badge = getActivity().findViewById(R.id.iv_badge);
            if (badge != null && tipsCompleted >= 5) {
                badge.setVisibility(View.VISIBLE);
                Toast.makeText(getActivity(), "Congratulations! You've earned the Eco-Warrior Badge!", Toast.LENGTH_LONG).show();
            }
        }
    }
}