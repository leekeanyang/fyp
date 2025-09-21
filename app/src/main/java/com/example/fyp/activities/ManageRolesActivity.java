package com.example.fyp.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp.R;
import com.example.fyp.adapters.UserAdapter;
import com.example.fyp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageRolesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private List<User> userList = new ArrayList<>();
    private UserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_roles);

        recyclerView = findViewById(R.id.recyclerViewUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();

        adapter = new UserAdapter(userList, this::onRoleChanged);
        recyclerView.setAdapter(adapter);

        loadUsers();
    }

    private void loadUsers() {
        db.collection("users")
                .whereNotEqualTo("role", "admin")
                .get()
                .addOnSuccessListener(query -> {
                    userList.clear();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        User user = doc.toObject(User.class);
                        userList.add(user);
                    }
                    adapter.notifyDataSetChanged();
                });
    }
    private void onRoleChanged(User user, String newRole) {
        db.collection("users").document(user.getUid())
                .update("role", newRole)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Role updated", Toast.LENGTH_SHORT).show();

                    // Log the change
                    Map<String, Object> logEntry = new HashMap<>();
                    logEntry.put("userId", user.getUid());
                    logEntry.put("changedBy", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    logEntry.put("newRole", newRole);
                    logEntry.put("timestamp", FieldValue.serverTimestamp());

                    db.collection("adminLogs").add(logEntry);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}

