package com.example.fyp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fyp.R;
import java.util.List;

public class RoleHistoryAdapter extends RecyclerView.Adapter<RoleHistoryAdapter.ViewHolder> {
    private List<String> roles;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String role);
    }

    public RoleHistoryAdapter(List<String> roles) {
        this.roles = roles;
        this.listener = listener;
    }

    public void updateData(List<String> newRoles) {
        this.roles = newRoles;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RoleHistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_role_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoleHistoryAdapter.ViewHolder holder, int position) {
        String role = roles.get(position);
        holder.tvRole.setText(role);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(role);
            }
        });
    }

    @Override
    public int getItemCount() {
        return roles == null ? 0 : roles.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRole;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRole = itemView.findViewById(R.id.tvRole);
        }
    }
}
