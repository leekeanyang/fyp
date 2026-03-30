package com.example.fyp;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fyp.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();

        ImageView splashBg = findViewById(R.id.splashBg);
        TextView title = findViewById(R.id.titleText);
        ProgressBar progressBar = findViewById(R.id.progressBar);
        ImageView leafParticles = findViewById(R.id.leafParticles);

        // Fade-in title
        title.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));

        // Zoom + vertical pan for background
        AnimationSet parallaxAnim = new AnimationSet(true);
        parallaxAnim.addAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_zoom));
        parallaxAnim.addAnimation(AnimationUtils.loadAnimation(this, R.anim.vertical_pan));
        splashBg.startAnimation(parallaxAnim);

        // Progress bar animation using ObjectAnimator
        ObjectAnimator progressAnimator = ObjectAnimator.ofInt(progressBar, "progress", 0, 100);
        progressAnimator.setDuration(2000);
        progressAnimator.start();

        // Leaf animation using ObjectAnimator
        AnimationDrawable leafAnimation = (AnimationDrawable) leafParticles.getDrawable();
        leafAnimation.start();

        ObjectAnimator fallY = ObjectAnimator.ofFloat(leafParticles, "translationY", 0f, 300f);
        ObjectAnimator fadeAlpha = ObjectAnimator.ofFloat(leafParticles, "alpha", 0.5f, 1f, 0.5f);
        AnimatorSet leafAnimator = new AnimatorSet();
        leafAnimator.playTogether(fallY, fadeAlpha);
        leafAnimator.setDuration(2000);
        leafAnimator.start();

        // Navigate after 2 seconds
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            SharedPreferences prefs = getSharedPreferences("loginPrefs", MODE_PRIVATE);
            boolean rememberMe = prefs.getBoolean("remember_me", false);

            Intent intent = (currentUser != null && rememberMe)
                    ? new Intent(SplashActivity.this, MainActivity.class)
                    : new Intent(SplashActivity.this, LoginActivity.class);

            startActivity(intent);
            finish();
        }, 2000);
    }
}
