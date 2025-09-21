package com.example.fyp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fyp.R;
import com.example.fyp.models.Report;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {
    private List<Report> reports;

    public ReportAdapter(List<Report> reports) {
        this.reports = reports;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Report report = reports.get(position);
        holder.issueType.setText(report.getIssueType());
        holder.description.setText(report.getDescription());
        holder.status.setText(report.getStatus());
        holder.location.setText(report.getLocation());

        // Format timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        holder.timestamp.setText(sdf.format(new Date(report.getTimestamp())));

        // Load photo if available
        if (report.getPhotoUrl() != null && !report.getPhotoUrl().isEmpty()) {
            Picasso.get().load(report.getPhotoUrl()).into(holder.photo);
            holder.photo.setVisibility(View.VISIBLE);
        } else {
            holder.photo.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView issueType, description, status, location, timestamp;
        ImageView photo;

        public ViewHolder(View itemView) {
            super(itemView);
            issueType = itemView.findViewById(R.id.report_issue_type);
            description = itemView.findViewById(R.id.report_description);
            status = itemView.findViewById(R.id.report_status);
            location = itemView.findViewById(R.id.report_location);
            timestamp = itemView.findViewById(R.id.report_timestamp);
            photo = itemView.findViewById(R.id.report_photo);
        }
    }
}