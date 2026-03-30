package com.example.fyp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fyp.R;
import com.example.fyp.auth.ForgotPasswordActivity;
import com.example.fyp.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import java.util.regex.Pattern;

public class AdminLoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private FirebaseAuth auth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        auth = FirebaseAuth.getInstance();
        etEmail = findViewById(R.id.et_admin_email);
        etPassword = findViewById(R.id.et_admin_password);
        Button btnLogin = findViewById(R.id.btn_login);

        // Initialize progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");
        progressDialog.setCancelable(false);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (validateInputs(email, password)) {
                progressDialog.show();
                btnLogin.setEnabled(false); // Prevent multiple clicks

                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            btnLogin.setEnabled(true); // Re-enable button
                            progressDialog.dismiss();

                            if (task.isSuccessful()) {
                                startActivity(new Intent(this, AdminDashboardActivity.class));
                                finish();
                            } else {
                                String errorMessage = "Login failed";
                                if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                                    errorMessage += ": Invalid email or user not found";
                                } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                    errorMessage += ": Incorrect password";
                                } else {
                                    errorMessage += ": " + task.getException().getMessage();
                                }
                                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            btnLogin.setEnabled(true); // Re-enable on failure
                            progressDialog.dismiss();
                            Toast.makeText(this, "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(AdminLoginActivity.this, ForgotPasswordActivity.class)));
    }

    private boolean validateInputs(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Email format validation
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        if (!Pattern.compile(emailPattern).matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Password strength (minimum 6 characters)
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}