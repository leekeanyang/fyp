package com.example.fyp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp.models.User;

import org.jspecify.annotations.NonNull;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public interface RoleChangeListener {
        void onRoleChange(User user, String newRole);
    }

    private final List<User> userList;
    private final RoleChangeListener listener;

    public UserAdapter(List<User> userList, RoleChangeListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.text1.setText(user.getEmail());
        holder.text2.setText("Role: " + user.getRole());

        holder.itemView.setOnClickListener(v -> showRoleDialog(holder.itemView.getContext(), user));
    }

    private void showRoleDialog(Context context, User user) {
        String[] roles = {"user", "moderator", "admin"};

        new AlertDialog.Builder(context)
                .setTitle("Change Role for " + user.getEmail())
                .setItems(roles, (dialog, which) -> {
                    String selectedRole = roles[which];
                    listener.onRoleChange(user, selectedRole);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView text1, text2;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }
    }
}

