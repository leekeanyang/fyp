package com.example.fyp.adapters;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

import com.example.fyp.R;

public class ZoomOutPageTransformer implements ViewPager2.PageTransformer {
    private static final float MIN_SCALE = 0.85f;
    private static final float MIN_ALPHA = 0.5f;

    @Override
    public void transformPage(@NonNull View page, float position) {
        int pageWidth = page.getWidth();
        int pageHeight = page.getHeight();

        // Zoom effect
        float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
        page.setScaleX(scaleFactor);
        page.setScaleY(scaleFactor);

        // Fade effect
        float alphaFactor = MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA);
        page.setAlpha(alphaFactor);

        // Parallax effect for image
        ImageView imageView = page.findViewById(R.id.image_view);
        if (imageView != null) {
            float parallaxSpeed = 0.3f;
            imageView.setTranslationX(-position * pageWidth * parallaxSpeed);
        }

        // Slide captions from bottom with fade
        LinearLayout captionLayout = page.findViewById(R.id.caption_layout);
        if (captionLayout != null) {
            // Slide up/down depending on swipe direction
            captionLayout.setTranslationY(position < 0 ? pageHeight * -position : pageHeight * position);
            captionLayout.setAlpha(alphaFactor);
        }
    }
}

