package com.example.fyp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp.R;
import com.example.fyp.activities.GuaTempurungActivity;
import com.example.fyp.activities.KualaSepetangActivity;
import com.example.fyp.activities.RoyalBelumActivity;

import java.util.Arrays;
import java.util.List;

public class DestinationsFragment extends Fragment {

    private List<Site> sites;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_destination, container, false);

        RecyclerView rvSites = view.findViewById(R.id.rv_sites);
        rvSites.setLayoutManager(new LinearLayoutManager(getContext()));

        // Define sites (expandable)
        sites = Arrays.asList(
                new Site("Royal Belum State Park", "Ancient rainforest with biodiversity hotspots.", RoyalBelumActivity.class),
                new Site("Gua Tempurung", "Limestone cave for adventure tours.", GuaTempurungActivity.class),
                new Site("Kuala Sepetang", "Mangrove forests and community tourism.", KualaSepetangActivity.class)
        );

        SiteAdapter adapter = new SiteAdapter(sites);
        rvSites.setAdapter(adapter);

        return view;
    }

    // Inner class for site data
    private static class Site {
        String name;
        String desc;
        Class<?> activityClass;

        Site(String name, String desc, Class<?> activityClass) {
            this.name = name;
            this.desc = desc;
            this.activityClass = activityClass;
        }
    }

    // Adapter
    private class SiteAdapter extends RecyclerView.Adapter<SiteAdapter.ViewHolder> {
        private List<Site> sites;

        SiteAdapter(List<Site> sites) {
            this.sites = sites;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_site, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Site site = sites.get(position);
            holder.tvName.setText(site.name);
            holder.tvDesc.setText(site.desc);
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), site.activityClass);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return sites.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvDesc;

            ViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
}