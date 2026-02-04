package com.example.fyp.activities;

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

    public static final String EXTRA_SITE = "extra_site";
    public static final String SITE_ROYAL_BELUM = "site_royal_belum";
    public static final String SITE_GUA_TEMPURUNG = "site_gua_tempurung";
    public static final String SITE_KUALA_SEPETANG = "site_kuala_sepetang";

    private ViewPager2 viewPager;
    private Timer timer;
    private int currentPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        viewPager = findViewById(R.id.view_pager);
        DotsIndicator dotsIndicator = findViewById(R.id.dots_indicator);

        String site = getIntent().getStringExtra(EXTRA_SITE);
        setTitle(getTitleForSite(site));
        List<String> images = getImagesForSite(site);
        List<String> captions = getCaptionsForSite(site);

        ImageAdapter adapter = new ImageAdapter(this, images, captions);
        viewPager.setAdapter(adapter);
        viewPager.setPageTransformer(new ZoomOutPageTransformer());

        dotsIndicator.setViewPager2(viewPager);

        autoSlide(3000); // Auto-slide every 3 seconds
    }

    private List<String> getImagesForSite(String site) {
        if (SITE_GUA_TEMPURUNG.equals(site)) {
            return Arrays.asList("gua_tempurung_1", "gua_tempurung_2", "gua_tempurung_3");
        }
        if (SITE_KUALA_SEPETANG.equals(site)) {
            return Arrays.asList("kuala_sepetang_1", "kuala_sepetang_2", "kuala_sepetang_3");
        }
        return Arrays.asList("royal_belum_1", "royal_belum_2", "royal_belum_3");
    }

    private List<String> getCaptionsForSite(String site) {
        if (SITE_GUA_TEMPURUNG.equals(site)) {
            return Arrays.asList("Gua Tempurung Entrance", "Limestone Chambers", "Underground Rivers");
        }
        if (SITE_KUALA_SEPETANG.equals(site)) {
            return Arrays.asList("Mangrove Walkways", "Firefly River Cruise", "Fishing Village Views");
        }
        return Arrays.asList("Rainforest Canopy", "Temenggor Lake", "Wildlife Habitat");
    }

    private String getTitleForSite(String site) {
        if (SITE_GUA_TEMPURUNG.equals(site)) {
            return "Gua Tempurung Gallery";
        }
        if (SITE_KUALA_SEPETANG.equals(site)) {
            return "Kuala Sepetang Gallery";
        }
        return "Royal Belum Gallery";
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
