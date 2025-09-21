package com.example.fyp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp.R;
import com.example.fyp.models.Site;

import java.util.List;

public class SiteAdapter extends RecyclerView.Adapter<SiteAdapter.SiteViewHolder> {

    private Context context;
    private List<Site> siteList;

    public SiteAdapter(Context context, List<Site> siteList) {
        this.context = context;
        this.siteList = siteList;
    }

    @NonNull
    @Override
    public SiteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_site, parent, false);
        return new SiteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SiteViewHolder holder, int position) {
        Site site = siteList.get(position);
        holder.siteName.setText(site.getName());
        holder.siteDesc.setText(site.getDescription());
        holder.siteImage.setImageResource(site.getImageResId());
    }

    @Override
    public int getItemCount() {
        return siteList.size();
    }

    public static class SiteViewHolder extends RecyclerView.ViewHolder {
        TextView siteName, siteDesc;
        ImageView siteImage;

        public SiteViewHolder(@NonNull View itemView) {
            super(itemView);
            siteName = itemView.findViewById(R.id.site_name);
            siteDesc = itemView.findViewById(R.id.site_desc);
            siteImage = itemView.findViewById(R.id.site_image);
        }
    }
}
