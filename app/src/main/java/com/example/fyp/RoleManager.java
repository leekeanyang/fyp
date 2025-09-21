package com.example.fyp;

import java.util.ArrayList;
import java.util.List;

public class RoleManager {
    private String currentRole;
    private List<String> roleHistory; // Undo stack (previous roles)
    private List<String> redoHistory; // Redo stack (roles undone)
    private static final int MAX_HISTORY_SIZE = 10;

    public RoleManager() {
        currentRole = "Guest";  // default role
        roleHistory = new ArrayList<>();
        redoHistory = new ArrayList<>();
    }

    public String getCurrentRole() {
        return currentRole;
    }

    public List<String> getRoleHistory() {
        return new ArrayList<>(roleHistory);
    }

    public List<String> getRedoHistory() {
        return new ArrayList<>(redoHistory);
    }

    /**
     * Set role as a new user action.
     * Clears redoHistory.
     */
    public void setRole(String newRole) {
        if (currentRole != null && !currentRole.equals(newRole)) {
            roleHistory.add(0, currentRole);
            trimHistory(roleHistory);
            redoHistory.clear();
        }
        currentRole = newRole;
        saveRoleAndHistories();
    }

    /**
     * Set role by selecting from undo or redo history.
     * @param newRole The selected role from history
     * @param isUndo true if from undo history, false if from redo history
     */
    public void setRoleFromHistory(String newRole, boolean isUndo) {
        if (currentRole == null || currentRole.equals(newRole)) return;

        if (isUndo) {
            // Undo path: currentRole moves to redo stack
            redoHistory.add(0, currentRole);
            trimHistory(redoHistory);
            roleHistory.remove(newRole); // Remove selected role from undo history
        } else {
            // Redo path: currentRole moves to undo stack
            roleHistory.add(0, currentRole);
            trimHistory(roleHistory);
            redoHistory.remove(newRole); // Remove selected role from redo history
        }
        currentRole = newRole;
        saveRoleAndHistories();
    }

    public void undoRoleChange() {
        if (roleHistory.isEmpty()) return;
        String prevRole = roleHistory.remove(0);
        redoHistory.add(0, currentRole);
        trimHistory(redoHistory);
        currentRole = prevRole;
        saveRoleAndHistories();
    }

    public void redoRoleChange() {
        if (redoHistory.isEmpty()) return;
        String nextRole = redoHistory.remove(0);
        roleHistory.add(0, currentRole);
        trimHistory(roleHistory);
        currentRole = nextRole;
        saveRoleAndHistories();
    }

    private void trimHistory(List<String> history) {
        if (history.size() > MAX_HISTORY_SIZE) {
            history.subList(MAX_HISTORY_SIZE, history.size()).clear();
        }
    }

    // Stub: Save currentRole, roleHistory, redoHistory to persistent storage (e.g. Firestore)
    private void saveRoleAndHistories() {
        // TODO: implement Firestore saving logic here
    }

    // Stub: Load currentRole, roleHistory, redoHistory from persistent storage
    public void loadUserRoleAndHistory(LoadCallback callback) {
        // TODO: implement Firestore loading logic here
        // For demo, just callback success immediately:
        callback.onSuccess(currentRole, new ArrayList<>(roleHistory), new ArrayList<>(redoHistory));
    }

    public interface LoadCallback {
        void onSuccess(String currentRole, List<String> undoHistory, List<String> redoHistory);
        void onFailure(String error);
    }
}
