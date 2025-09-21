package com.example.fyp.activities;

import android.os.Bundle;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fyp.R;

public class ExpandableCardActivity extends AppCompatActivity {

    private boolean isExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_expandable_card);

        LinearLayout headerLayout = findViewById(R.id.headerLayout);
        LinearLayout contentLayout = findViewById(R.id.contentLayout);
        ImageView arrowIcon = findViewById(R.id.arrowIcon);

        headerLayout.setOnClickListener(v -> {
            if (isExpanded) {
                // Collapse
                contentLayout.setVisibility(View.GONE);
                rotateArrow(arrowIcon, 180f, 0f);
            } else {
                // Expand
                contentLayout.setVisibility(View.VISIBLE);
                rotateArrow(arrowIcon, 0f, 180f);
            }
            isExpanded = !isExpanded;
        });
    }

    private void rotateArrow(ImageView arrow, float from, float to) {
        RotateAnimation rotate = new RotateAnimation(from, to,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(300);
        rotate.setFillAfter(true);
        arrow.startAnimation(rotate);
    }
}
