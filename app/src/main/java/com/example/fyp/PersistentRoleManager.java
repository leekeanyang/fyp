package com.example.fyp;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class PersistentRoleManager {
    private final FirebaseFirestore db;
    private final String userId;

    public PersistentRoleManager() {
        this.db = FirebaseFirestore.getInstance();
        this.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public void setRole(String newRole) {
        DocumentReference userRef = db.collection("users").document(userId);

        db.runTransaction(transaction -> {
                    // Get current role
                    String currentRole = transaction.get(userRef).getString("role");

                    if (currentRole != null && !currentRole.equals(newRole)) {
                        // Push current role into roleHistory
                        transaction.update(userRef, "roleHistory", FieldValue.arrayUnion(currentRole));
                        // Update to new role
                        transaction.update(userRef, "role", newRole);
                    }
                    return null;
                }).addOnSuccessListener(aVoid -> Log.d("RoleManager", "Role updated to: " + newRole))
                .addOnFailureListener(e -> Log.e("RoleManager", "Error updating role", e));
    }

    public void undoRoleChange() {
        DocumentReference userRef = db.collection("users").document(userId);

        db.runTransaction(transaction -> {
                    // Get role history
                    List<String> history = (List<String>) transaction.get(userRef).get("roleHistory");
                    if (history != null && !history.isEmpty()) {
                        // Get last role
                        String previousRole = history.get(history.size() - 1);

                        // Remove it from history
                        transaction.update(userRef, "roleHistory", FieldValue.arrayRemove(previousRole));

                        // Update role to previous
                        transaction.update(userRef, "role", previousRole);
                    }
                    return null;
                }).addOnSuccessListener(aVoid -> Log.d("RoleManager", "Undo successful"))
                .addOnFailureListener(e -> Log.e("RoleManager", "Error undoing role", e));
    }
}
