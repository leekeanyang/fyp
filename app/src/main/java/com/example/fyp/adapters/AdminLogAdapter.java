package com.example.fyp.adapters;

import android.app.AlertDialog;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp.R;
import com.example.fyp.models.AdminLog;

import org.jspecify.annotations.NonNull;

import java.text.DateFormat;
import java.util.List;

public class AdminLogAdapter extends RecyclerView.Adapter<AdminLogAdapter.LogViewHolder> {

    public interface UndoClickListener {
        void onUndoClicked(AdminLog log);
    }

    private final List<AdminLog> logs;
    private final UndoClickListener listener;
    private String searchQuery = "";

    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_LOADING = 1;

    private boolean isLoadingFooterAdded = false;


    public AdminLogAdapter(List<AdminLog> logs, UndoClickListener listener) {
        this.logs = logs;
        this.listener = listener;
    }

    public void setSearchQuery(String query) {
        this.searchQuery = query == null ? "" : query.toLowerCase();
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        AdminLog log = logs.get(position);

        String userId = log.getUserId() != null ? log.getUserId() : "";
        String changedBy = log.getChangedBy() != null ? log.getChangedBy() : "";
        String newRole = log.getNewRole() != null ? log.getNewRole() : "";

        String text1 = "User: " + userId + " Role: " + newRole;
        String text2 = "Changed by: " + changedBy +
                " at " + DateFormat.getDateTimeInstance().format(log.getTimestamp());

        holder.text1.setText(getHighlightedText(text1, searchQuery));
        holder.text2.setText(getHighlightedText(text2, searchQuery));

        holder.itemView.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Undo Role Change")
                    .setMessage("Reset this user's role to 'user'?")
                    .setPositiveButton("Yes", (dialog, which) -> listener.onUndoClicked(log))
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return logs.size() + (isLoadingFooterAdded ? 1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        return (position == logs.size() && isLoadingFooterAdded) ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }
    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;
        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView text1, text2;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }
    }

    private Spannable getHighlightedText(String fullText, String query) {
        Spannable spannable = Spannable.Factory.getInstance().newSpannable(fullText);
        if (query == null || query.isEmpty()) return spannable;

        String lowerText = fullText.toLowerCase();
        int index = lowerText.indexOf(query);
        while (index >= 0) {
            // Highlight with yellow background
            spannable.setSpan(new BackgroundColorSpan(0xFFFFFF00), index, index + query.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Search for next match
            index = lowerText.indexOf(query, index + query.length());
        }
        return spannable;
    }
    public void addLoadingFooter() {
        if (!isLoadingFooterAdded) {
            isLoadingFooterAdded = true;
            notifyItemInserted(logs.size());
        }
    }

    public void removeLoadingFooter() {
        if (isLoadingFooterAdded) {
            isLoadingFooterAdded = false;
            notifyItemRemoved(logs.size());
        }
    }

}
