package com.example.fyp.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fyp.R;
import com.example.fyp.RoleManager;
import com.example.fyp.adapters.RoleHistoryAdapter;

import java.util.ArrayList;
import java.util.List;

public class RoleManagerActivity extends AppCompatActivity {
    private RoleManager roleManager;

    private TextView tvCurrentRole;
    private Button btnSetRole, btnUndo, btnRedo;
    private RecyclerView rvUndoHistory, rvRedoHistory;

    private RoleHistoryAdapter undoAdapter, redoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_manager);

        tvCurrentRole = findViewById(R.id.tvCurrentRole);
        btnSetRole = findViewById(R.id.btnSetRole);
        btnUndo = findViewById(R.id.btnUndo);
        btnRedo = findViewById(R.id.btnRedo);
        rvUndoHistory = findViewById(R.id.rvUndoHistory);
        rvRedoHistory = findViewById(R.id.rvRedoHistory);

        roleManager = new RoleManager();

        undoAdapter = new RoleHistoryAdapter(new ArrayList<>());
        redoAdapter = new RoleHistoryAdapter(new ArrayList<>());

        rvUndoHistory.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvRedoHistory.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        rvUndoHistory.setAdapter(undoAdapter);
        rvRedoHistory.setAdapter(redoAdapter);

        loadData();

        btnSetRole.setOnClickListener(v -> {
            final EditText input = new EditText(this);
            input.setHint("Enter new role");

            new AlertDialog.Builder(this)
                    .setTitle("Set New Role")
                    .setView(input)
                    .setPositiveButton("OK", (dialog, which) -> {
                        String newRole = input.getText().toString().trim();
                        if (!newRole.isEmpty()) {
                            roleManager.setRole(newRole);
                            loadData();
                        } else {
                            Toast.makeText(this, "Role cannot be empty", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        btnUndo.setOnClickListener(v -> {
            roleManager.undoRoleChange();
            loadData();
        });

        btnRedo.setOnClickListener(v -> {
            roleManager.redoRoleChange();
            loadData();
        });
    }

    private void loadData() {
        roleManager.loadUserRoleAndHistory(new RoleManager.LoadCallback() {
            @Override
            public void onSuccess(String role, List<String> history, List<String> redo) {
                runOnUiThread(() -> {
                    tvCurrentRole.setText("Current Role: " + role);
                    undoAdapter.updateData(history);
                    redoAdapter.updateData(redo);
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> Toast.makeText(RoleManagerActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show());
            }
        });
    }
}
