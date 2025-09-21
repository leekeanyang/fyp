package com.example.fyp.activities;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.fyp.R;
import com.example.fyp.adapters.ImageAdapter;
import com.example.fyp.adapters.ZoomOutPageTransformer;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class GalleryActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private Timer timer;
    private int currentPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        viewPager = findViewById(R.id.view_pager);
        DotsIndicator dotsIndicator = findViewById(R.id.dots_indicator);

        // Sample images & captions
        List<String> images = Arrays.asList("royal_belum_1", "gua_tempurung_1", "kuala_sepetang_1");
        List<String> captions = Arrays.asList("Royal Belum Rainforest", "Gua Tempurung Caves", "Kuala Sepetang Mangrove");

        ImageAdapter adapter = new ImageAdapter((Context) images, captions);
        viewPager.setAdapter(adapter);
        viewPager.setPageTransformer(new ZoomOutPageTransformer());

        dotsIndicator.setViewPager2(viewPager);

        autoSlide(3000); // Auto-slide every 3 seconds
    }

    private void autoSlide(int interval) {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    int pageCount = viewPager.getAdapter().getItemCount();
                    currentPage = (currentPage + 1) % pageCount;
                    viewPager.setCurrentItem(currentPage, true);
                });
            }
        }, interval, interval);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
    }
}
