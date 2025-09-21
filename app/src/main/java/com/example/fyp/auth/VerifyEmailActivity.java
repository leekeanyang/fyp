package com.example.fyp.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fyp.MainActivity;
import com.example.fyp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class VerifyEmailActivity extends AppCompatActivity {

    private TextView tvVerifyMsg;
    private Button btnResend, btnCheckStatus, btnLogout;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        tvVerifyMsg = findViewById(R.id.tvVerifyMsg);
        btnResend = findViewById(R.id.btnResend);
        btnCheckStatus = findViewById(R.id.btnCheckStatus);
        btnLogout = findViewById(R.id.btnLogout);

        btnResend.setOnClickListener(v -> resendVerificationEmail());
        btnCheckStatus.setOnClickListener(v -> checkVerificationStatus());
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(VerifyEmailActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void resendVerificationEmail() {
        if (user != null) {
            btnResend.setEnabled(false);
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        btnResend.setEnabled(true);
                        if (task.isSuccessful()) {
                            Toast.makeText(VerifyEmailActivity.this,
                                    "Verification email sent.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(VerifyEmailActivity.this,
                                    "Failed to send verification email.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void checkVerificationStatus() {
        if (user != null) {
            btnCheckStatus.setEnabled(false);
            user.reload().addOnCompleteListener(task -> {
                btnCheckStatus.setEnabled(true);
                if (user.isEmailVerified()) {
                    Toast.makeText(VerifyEmailActivity.this,
                            "Email verified! Setting up your profile...",
                            Toast.LENGTH_SHORT).show();
                    updateUserProfileThenProceed();
                } else {
                    Toast.makeText(VerifyEmailActivity.this,
                            "Email not verified yet. Please check your inbox.",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateUserProfileThenProceed() {
        // Example: set a default display name if none exists
        if (user != null && (user.getDisplayName() == null || user.getDisplayName().isEmpty())) {
            user.updateProfile(new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName("EcoTourist")
                    .build()).addOnCompleteListener(task -> {
                startMainActivity();
            });
        } else {
            startMainActivity();
        }
    }

    private void startMainActivity() {
        startActivity(new Intent(VerifyEmailActivity.this, MainActivity.class));
        finish();
    }
}
