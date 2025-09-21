package com.example.fyp.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.fyp.R;
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
        siteList.add(new Site("Royal Belum State Park",
                "One of the oldest rainforests in the world, home to tigers, elephants, and rare plants.",
                R.drawable.royal_belum));
        siteList.add(new Site("Gua Tempurung",
                "Largest limestone cave in Peninsular Malaysia with stunning rock formations.",
                R.drawable.gua_tempurung));
        siteList.add(new Site("Kuala Sepetang Mangrove",
                "Famous for mangrove forests, fireflies, and seafood village culture.",
                R.drawable.kuala_sepetang));

        SiteAdapter adapter = new SiteAdapter(getContext(), siteList);
        recyclerView.setAdapter(adapter);

        return view;
    }
}
