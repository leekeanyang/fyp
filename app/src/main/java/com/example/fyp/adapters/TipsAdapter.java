package com.example.fyp.adapters;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp.R;
import com.example.fyp.models.Tip;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.Locale;

public class TipsAdapter extends RecyclerView.Adapter<TipsAdapter.TipViewHolder> {

    private List<Tip> tips;
    private OnTipCompletedListener listener;
    private static Context context;
    private int lastPosition = -1; // For staggered animations

    public TipsAdapter(List<Tip> tips, OnTipCompletedListener listener) {
        this.tips = tips;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tip, parent, false);
        context = parent.getContext();
        return new TipViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TipViewHolder holder, int position) {
        Tip tip = tips.get(position);
        holder.bind(tip);
        setAnimation(holder.itemView, position); // Apply staggered animation
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTipCompleted(tip);
                holder.showCompletionFeedback(tip); // Show feedback
            }
        });
    }

    @Override
    public int getItemCount() {
        return tips != null ? tips.size() : 0;
    }

    public void updateTips(List<Tip> newTips) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return tips != null ? tips.size() : 0; }
            @Override public int getNewListSize() { return newTips != null ? newTips.size() : 0; }
            @Override public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return tips.get(oldItemPosition).getId().equals(newTips.get(newItemPosition).getId());
            }
            @Override public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return tips.get(oldItemPosition).equals(newTips.get(newItemPosition));
            }
        });
        this.tips = newTips;
        diffResult.dispatchUpdatesTo(this);
    }

    private void setAnimation(View view, int position) {
        if (position > lastPosition) {
            view.setAlpha(0f);
            view.animate().alpha(1f).setDuration(300).setStartDelay(position * 100).start();
            lastPosition = position;
        }
    }

    public static class TipViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTipTitle, tvTipDescription, tvTipSite;
        private ImageView ivTipImage, ivSpeak;
        private TextToSpeech tts;

        public TipViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTipTitle = itemView.findViewById(R.id.tv_tip_title);
            tvTipDescription = itemView.findViewById(R.id.tv_tip_description);
            tvTipSite = itemView.findViewById(R.id.tv_tip_site);
            ivTipImage = itemView.findViewById(R.id.iv_tip_image);
            ivSpeak = itemView.findViewById(R.id.iv_speak);
            tts = new TextToSpeech(itemView.getContext(), status -> {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(Locale.getDefault());
                    tts.setSpeechRate(0.9f); // Slightly slower for clarity
                } else {
                    Toast.makeText(itemView.getContext(), "TTS initialization failed", Toast.LENGTH_SHORT).show();
                }
            });
            setupSpeakButton();
        }

        private void setupSpeakButton() {
            ivSpeak.setOnClickListener(v -> {
                if (tts != null && tvTipTitle.getText() != null && tvTipDescription.getText() != null) {
                    String textToSpeak = tvTipTitle.getText() + ". " + tvTipDescription.getText();
                    tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
                    v.startAnimation(AnimationUtils.loadAnimation(context, android.R.anim.fade_in)); // Feedback animation
                } else {
                    Toast.makeText(context, "Text-to-speech unavailable", Toast.LENGTH_SHORT).show();
                }
            });
        }

        public void bind(Tip tip) {
            tvTipTitle.setText(tip.getTitle());
            tvTipDescription.setText(tip.getDescription());
            tvTipSite.setText(tip.getSite() != null ? "Site: " + tip.getSite() : "");
            tvTipSite.setVisibility(tip.getSite() != null ? View.VISIBLE : View.GONE);

            String imageName = tip.getImageUrl();
            if (imageName != null && !imageName.isEmpty()) {
                int resId = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
                Glide.with(context)
                        .load(resId != 0 ? resId : R.drawable.ic_eco_tip)
                        .placeholder(R.drawable.ic_eco_tip_placeholder)
                        .error(R.drawable.ic_eco_tip)
                        .into(ivTipImage);
            } else {
                Glide.with(context).load(R.drawable.ic_eco_tip).into(ivTipImage); // Default image
            }

            // Accessibility: Set content descriptions
            ivTipImage.setContentDescription(tip.getTitle() + " illustration");
            ivSpeak.setContentDescription("Listen to " + tip.getTitle());
        }

        public void showCompletionFeedback(Tip tip) {
            itemView.setBackgroundColor(context.getResources().getColor(R.color.green, context.getTheme()));
            itemView.postDelayed(() -> itemView.setBackgroundResource(android.R.color.transparent), 500); // Flash effect
            Snackbar.make(itemView, "Completed: " + tip.getTitle(), Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(context.getResources().getColor(R.color.eco_green, context.getTheme()))
                    .setTextColor(context.getResources().getColor(android.R.color.white))
                    .show();
        }

        @Override
        protected void finalize() throws Throwable {
            if (tts != null) {
                tts.stop();
                tts.shutdown();
            }
            super.finalize();
        }
    }

    public interface OnTipCompletedListener {
        void onTipCompleted(Tip tip);
    }
}