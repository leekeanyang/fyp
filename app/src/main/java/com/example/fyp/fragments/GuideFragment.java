package com.example.fyp.fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.fyp.R;
import com.example.fyp.activities.SiteDetailActivity;
import com.example.fyp.adapters.SiteAdapter;
import com.example.fyp.models.Site;

import java.util.ArrayList;
import java.util.List;

public class GuideFragment extends Fragment {

    RecyclerView recyclerView;
    List<Site> siteList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_guide, container, false);

        recyclerView = view.findViewById(R.id.recycler_sites);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        siteList = new ArrayList<>();
        
        // Detailed Site data for Perak ecotourism sites
        siteList.add(new Site(
                "Royal Belum State Park",
                "One of the oldest rainforests in the world, home to rare plants.",
                "Royal Belum State Park is one of the oldest rainforests in the world, over 130 million years old. Rich in biodiversity, it offers eco-adventures such as jungle trekking and wildlife spotting.",
                "• All 10 species of Malaysian Hornbills\n• Rafflesia flowers\n• Malayan tigers and elephants",
                "• Keep to designated trails\n• No single-use plastics allowed\n• Respect local Orang Asli customs",
                "• Restricted access to protected core zones\n• Permit required 1 week in advance",
                R.drawable.royal_belum, 5.9466, 101.3594
        ));

        siteList.add(new Site(
                "Gua Tempurung",
                "Largest limestone cave in Peninsular Malaysia.",
                "Gua Tempurung is a magnificent limestone cave system stretching over 3km. It features massive chambers and breath-taking stalactites and stalagmites.",
                "• Ancient limestone formations\n• Underground river systems\n• Unique cave fauna",
                "• Wear non-slip shoes\n• Use waterproof bags for electronics\n• Do not touch rock formations",
                "• Wet tours restricted during heavy rain\n• Some chambers are physically demanding",
                R.drawable.gua_tempurung, 4.4148, 101.1878
        ));

        siteList.add(new Site(
                "Kuala Sepetang Mangrove",
                "Mangrove forests, fireflies, and seafood village culture.",
                "The Matang Mangrove Forest Reserve in Kuala Sepetang is the best-managed mangrove forest in the world. It plays a crucial role in coastal protection and biodiversity.",
                "• Diverse mangrove tree species\n• Migratory birds and fireflies\n• Traditional charcoal kilns",
                "• Do not use flash photography with fireflies\n• Avoid littering in the waterways\n• Use eco-friendly sunscreen",
                "• High tide warnings during monsoon\n• Beware of monkeys near the boardwalks",
                R.drawable.kuala_sepetang, 4.8371, 100.6267
        ));

        SiteAdapter adapter = new SiteAdapter(getContext(), siteList);
        recyclerView.setAdapter(adapter);

        return view;
    }
}
