package com.example.fyp.auth;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.example.fyp.MainActivity;
import com.example.fyp.R;
import com.example.fyp.activities.AdminLoginActivity; // Import AdminLoginActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.concurrent.Executor;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout emailLayout, passwordLayout;
    private TextInputEditText emailInput, passwordInput;
    private Button btnLogin, btnBiometric, btnGoogle;
    private CheckBox rememberMeCheckBox;
    private ProgressBar loginProgress;
    private FirebaseAuth mAuth;
    private SharedPreferences prefs;

    // Google Sign-In
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        prefs = getSharedPreferences("loginPrefs", MODE_PRIVATE);

        // UI references
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        btnLogin = findViewById(R.id.btnLogin);
        btnBiometric = findViewById(R.id.btnBiometric);
        btnGoogle = findViewById(R.id.btnGoogle);
        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox);
        loginProgress = findViewById(R.id.loginProgress);

        // Restore saved email if Remember Me was checked
        String savedEmail = prefs.getString("email", null);
        if (savedEmail != null) {
            emailInput.setText(savedEmail);
        }
        rememberMeCheckBox.setChecked(prefs.getBoolean("remember_me", savedEmail != null));

        // Real-time validation
        TextWatcher validator = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateEmail();
                validatePassword();
            }
            @Override public void afterTextChanged(Editable s) {}
        };
        emailInput.addTextChangedListener(validator);
        passwordInput.addTextChangedListener(validator);

        // Navigation links
        TextView tvRegisterLink = findViewById(R.id.tvRegisterLink);
        tvRegisterLink.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));

        // Add Admin Login link
        TextView tvAdminLogin = findViewById(R.id.tvAdminLogin); // Reference the new TextView
        tvAdminLogin.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, AdminLoginActivity.class)));

        // Buttons
        btnLogin.setOnClickListener(v -> performLogin());
        setupBiometricLogin();
        setupGoogleLogin();
    }

    private void validateEmail() {
        String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
        if (TextUtils.isEmpty(email)) {
            emailLayout.setError("Email is required");
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Invalid email format");
        } else {
            emailLayout.setError(null);
        }
    }

    private void validatePassword() {
        String password = passwordInput.getText() != null ? passwordInput.getText().toString() : "";
        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError("Password is required");
        } else if (password.length() < 6) {
            passwordLayout.setError("Password must be at least 6 characters");
        } else {
            passwordLayout.setError(null);
        }
    }

    private void performLogin() {
        // Final validation before login
        validateEmail();
        validatePassword();

        String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
        String password = passwordInput.getText() != null ? passwordInput.getText().toString() : "";

        if (emailLayout.getError() != null || passwordLayout.getError() != null) {
            return;
        }

        loginProgress.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    loginProgress.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);
                    if (task.isSuccessful()) {
                        // Enable biometric login and save flag
                        prefs.edit().putBoolean("biometric_enabled", true).apply();
                        // Save email if Remember Me is checked
                        if (rememberMeCheckBox.isChecked()) {
                            prefs.edit().putString("email", email).apply();
                        } else {
                            prefs.edit().remove("email").apply();
                        }
                        prefs.edit().putBoolean("remember_me", rememberMeCheckBox.isChecked()).apply();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        if (errorMessage.contains("wrong-password")) {
                            passwordLayout.setError("Incorrect password");
                        } else if (errorMessage.contains("user-not-found")) {
                            emailLayout.setError("User not found");
                        } else {
                            Toast.makeText(LoginActivity.this, "Login failed: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    // ----------------- BIOMETRIC LOGIN -----------------
    private void setupBiometricLogin() {
        BiometricManager biometricManager = BiometricManager.from(this);
        if (biometricManager.canAuthenticate() != BiometricManager.BIOMETRIC_SUCCESS) {
            btnBiometric.setVisibility(View.GONE);
            return;
        }

        // Check if biometric is enabled (after initial login)
        if (!prefs.getBoolean("biometric_enabled", false)) {
            btnBiometric.setVisibility(View.GONE);
            return;
        }

        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Toast.makeText(LoginActivity.this, "Biometric authentication failed", Toast.LENGTH_SHORT).show();
                    }
                });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Login")
                .setSubtitle("Login using your fingerprint or face")
                .setDeviceCredentialAllowed(true)
                .build();

        btnBiometric.setOnClickListener(v -> biometricPrompt.authenticate(promptInfo));
    }

    // ----------------- GOOGLE LOGIN -----------------
    private void setupGoogleLogin() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            if (account != null) {
                                firebaseAuthWithGoogle(account.getIdToken());
                            }
                        } catch (ApiException e) {
                            Toast.makeText(this, "Google sign-in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        btnGoogle.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Enable biometric login and save flag
                        prefs.edit().putBoolean("biometric_enabled", true).apply();
                        prefs.edit().putBoolean("remember_me", true).apply();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Google Authentication Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public void onBackPressed() {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        super.onBackPressed();
    }
}
