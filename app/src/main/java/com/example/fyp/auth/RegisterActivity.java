package com.example.fyp.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fyp.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout emailLayout, passwordLayout, passwordConfirmLayout;
    private TextInputEditText emailInput, passwordInput, passwordConfirmInput;
    private Button btnRegister;
    private ProgressBar registerProgress;
    private FirebaseAuth mAuth;
    private TextView txtLoginRedirect; // 👈 add reference

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        passwordConfirmLayout = findViewById(R.id.passwordConfirmLayout);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        passwordConfirmInput = findViewById(R.id.passwordConfirmInput);

        btnRegister = findViewById(R.id.btnRegister);
        registerProgress = findViewById(R.id.registerProgress);
        txtLoginRedirect = findViewById(R.id.txtLoginRedirect); // 👈 init here

        mAuth = FirebaseAuth.getInstance();

        btnRegister.setOnClickListener(v -> registerUser());

        // 👇 Redirect to LoginActivity
        txtLoginRedirect.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {
        emailLayout.setError(null);
        passwordLayout.setError(null);
        passwordConfirmLayout.setError(null);

        String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
        String password = passwordInput.getText() != null ? passwordInput.getText().toString() : "";
        String passwordConfirm = passwordConfirmInput.getText() != null ? passwordConfirmInput.getText().toString() : "";

        boolean cancel = false;

        // Validate email
        if (TextUtils.isEmpty(email)) {
            emailLayout.setError("Email is required");
            cancel = true;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Invalid email format");
            cancel = true;
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError("Password is required");
            cancel = true;
        } else if (password.length() < 6) {
            passwordLayout.setError("Password must be at least 6 characters");
            cancel = true;
        }

        // Validate password confirmation
        if (!password.equals(passwordConfirm)) {
            passwordConfirmLayout.setError("Passwords do not match");
            cancel = true;
        }

        if (cancel) {
            return;
        }

        registerProgress.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    registerProgress.setVisibility(View.GONE);
                    btnRegister.setEnabled(true);
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Registration successful! Please verify your email.", Toast.LENGTH_LONG).show();

                        if (mAuth.getCurrentUser() != null) {
                            mAuth.getCurrentUser().sendEmailVerification();
                        }

                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this,
                                "Registration failed: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
