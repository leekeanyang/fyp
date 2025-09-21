package com.example.fyp.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.fyp.activities.AdminDashboardActivity;
import com.example.fyp.MainActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class FirebaseAuthHelper {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private Activity activity;
    private static final int RC_SIGN_IN = 9001;

    public FirebaseAuthHelper(Activity activity) {
        this.activity = activity;
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);
    }

    public void handleGoogleSignInResult(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        String uid = user.getUid();

                        db.collection("users").document(uid).get()
                                .addOnSuccessListener(document -> {
                                    if (document.exists()) {
                                        String role = document.getString("role");

                                        if ("admin".equalsIgnoreCase(role)) {
                                            activity.startActivity(new Intent(activity, AdminDashboardActivity.class));
                                        } else {
                                            activity.startActivity(new Intent(activity, MainActivity.class));
                                        }

                                        activity.finish();
                                    } else {
                                        Toast.makeText(activity, "User role not found.", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(activity, "Error fetching role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(activity, "Auth Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users").document(uid)
                .set(new HashMap<String, Object>() {{
                    put("email", FirebaseAuth.getInstance().getCurrentUser().getEmail());
                    put("role", "user"); // or "admin"
                }});
    }
}

