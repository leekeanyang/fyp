package com.example.fyp;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersistentRoleManagerWithTime {
    private final FirebaseFirestore db;
    private final String userId;

    public PersistentRoleManagerWithTime() {
        this.db = FirebaseFirestore.getInstance();
        this.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public void setRole(String newRole) {
        DocumentReference userRef = db.collection("users").document(userId);

        db.runTransaction(transaction -> {
            String currentRole = transaction.get(userRef).getString("role");

            if (currentRole != null && !currentRole.equals(newRole)) {
                // Create history entry with timestamp
                Map<String, Object> historyEntry = new HashMap<>();
                historyEntry.put("role", currentRole);
                historyEntry.put("changedAt", Timestamp.now());

                // Push to roleHistory
                transaction.update(userRef, "roleHistory", FieldValue.arrayUnion(historyEntry));
                // Update role
                transaction.update(userRef, "role", newRole);
            }
            return null;
        }).addOnSuccessListener(aVoid ->
                Log.d("RoleManager", "Role updated to: " + newRole)
        ).addOnFailureListener(e ->
                Log.e("RoleManager", "Error updating role", e)
        );
    }

    public void undoRoleChange() {
        DocumentReference userRef = db.collection("users").document(userId);

        db.runTransaction(transaction -> {
            List<Map<String, Object>> history =
                    (List<Map<String, Object>>) transaction.get(userRef).get("roleHistory");

            if (history != null && !history.isEmpty()) {
                // Get last entry
                Map<String, Object> lastEntry = history.get(history.size() - 1);
                String previousRole = (String) lastEntry.get("role");

                // Remove last entry from history
                transaction.update(userRef, "roleHistory", FieldValue.arrayRemove(lastEntry));

                // Update role to previous
                transaction.update(userRef, "role", previousRole);
            }
            return null;
        }).addOnSuccessListener(aVoid ->
                Log.d("RoleManager", "Undo successful")
        ).addOnFailureListener(e ->
                Log.e("RoleManager", "Error undoing role", e)
        );
    }
}

