package com.example.fyp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp.R;
import com.example.fyp.models.Tip;
import com.bumptech.glide.Glide;

import java.util.List;

public class TipsAdapter extends RecyclerView.Adapter<TipsAdapter.TipViewHolder> {

    private List<Tip> tips;
    private OnTipActionListener listener;
    private Context context;
    private int lastPosition = -1;

    public interface OnTipActionListener {
        void onTipCompleted(Tip tip);
        void onSpeakTip(Tip tip);
    }

    public TipsAdapter(List<Tip> tips, OnTipActionListener listener) {
        this.tips = tips;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_tip, parent, false);
        return new TipViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TipViewHolder holder, int position) {
        Tip tip = tips.get(position);
        holder.bind(tip, listener);
        setAnimation(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return tips != null ? tips.size() : 0;
    }

    public void updateTips(final List<Tip> newTips) {
        if (tips == null) {
            tips = newTips;
            notifyItemRangeInserted(0, newTips.size());
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return tips.size();
                }

                @Override
                public int getNewListSize() {
                    return newTips.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return tips.get(oldItemPosition).getId().equals(newTips.get(newItemPosition).getId());
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    return tips.get(oldItemPosition).equals(newTips.get(newItemPosition));
                }
            });
            tips = newTips;
            result.dispatchUpdatesTo(this);
        }
    }

    private void setAnimation(View view, int position) {
        if (position > lastPosition) {
            view.setAlpha(0f);
            view.animate().alpha(1f).setDuration(300).setStartDelay(position * 50L).start();
            lastPosition = position;
        }
    }

    public static class TipViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTipTitle, tvTipDescription, tvTipSite;
        private ImageView ivTipImage, ivSpeak, ivCheck;

        public TipViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTipTitle = itemView.findViewById(R.id.tv_tip_title);
            tvTipDescription = itemView.findViewById(R.id.tv_tip_description);
            tvTipSite = itemView.findViewById(R.id.tv_tip_site);
            ivTipImage = itemView.findViewById(R.id.iv_tip_image);
            ivSpeak = itemView.findViewById(R.id.iv_speak);
            ivCheck = itemView.findViewById(R.id.iv_check);
        }

        public void bind(Tip tip, OnTipActionListener listener) {
            tvTipTitle.setText(tip.getTitle());
            tvTipDescription.setText(tip.getDescription());
            tvTipSite.setText(tip.getSite() != null ? "Site: " + tip.getSite() : "");
            tvTipSite.setVisibility(tip.getSite() != null && !tip.getSite().isEmpty() ? View.VISIBLE : View.GONE);
            
            // Show check icon if completed
            if (ivCheck != null) {
                ivCheck.setVisibility(tip.isCompleted() ? View.VISIBLE : View.GONE);
            }

            Context context = itemView.getContext();
            String imageName = tip.getImageUrl();
            if (imageName != null && !imageName.isEmpty()) {
                int resId = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
                Glide.with(context)
                        .load(resId != 0 ? resId : R.drawable.ic_eco_tip)
                        .placeholder(R.drawable.ic_eco_tip_placeholder)
                        .error(R.drawable.ic_eco_tip)
                        .into(ivTipImage);
            } else {
                Glide.with(context).load(R.drawable.ic_eco_tip).into(ivTipImage);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTipCompleted(tip);
                    if (!tip.isCompleted()) {
                        showCompletionFeedback(context);
                    }
                }
            });

            ivSpeak.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSpeakTip(tip);
                    v.startAnimation(AnimationUtils.loadAnimation(context, android.R.anim.fade_in));
                }
            });
        }

        private void showCompletionFeedback(Context context) {
            itemView.setBackgroundColor(context.getResources().getColor(R.color.green, context.getTheme()));
            itemView.postDelayed(() -> itemView.setBackgroundResource(android.R.color.transparent), 500);
        }
    }
}
