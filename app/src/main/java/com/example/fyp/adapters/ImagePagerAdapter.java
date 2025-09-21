package com.example.fyp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.fyp.R;

import java.util.Arrays;

public class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ViewHolder> {
    private String[] images;
    private OnImageClickListener listener;

    public interface OnImageClickListener {
        void onImageClick(String imageUrl, int position);
    }

    public ImagePagerAdapter(String[] images, OnImageClickListener listener) {
        this.images = images != null ? images : new String[0];
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String imageUrl = images[position];
        // Set dynamic content description
        holder.image.setContentDescription(
                holder.itemView.getContext().getString(R.string.image_description_template, position + 1));

        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .thumbnail(0.25f) // Load a low-res thumbnail first
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image) // Add error placeholder
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache both original and transformed images
                .into(holder.image);

        holder.image.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImageClick(imageUrl, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return images.length;
    }

    // Update image list dynamically
    public void updateImages(String[] newImages) {
        this.images = newImages != null ? newImages : new String[0];
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}