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
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private final List<String> imageNames; // names of drawables or vectors
    private final List<String> captions;
    private final Context context;

    public ImageAdapter(Context context, List<String> imageNames, List<String> captions) {
        this.context = context;
        this.imageNames = imageNames;
        this.captions = captions;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageName = imageNames.get(position);

        // Try to get drawable ID
        int resId = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());

        if (resId != 0) {
            holder.imageView.setImageResource(resId);
        } else {
            // Fallback placeholder if drawable not found
            holder.imageView.setImageResource(R.drawable.ic_placeholder);
        }

        if (captions != null && captions.size() > position) {
            holder.captionText.setText(captions.get(position));
        } else {
            holder.captionText.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return imageNames.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView captionText;
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            captionText = itemView.findViewById(R.id.caption_text);
        }
    }
}
