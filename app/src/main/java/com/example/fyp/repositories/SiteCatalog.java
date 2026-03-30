package com.example.fyp.repositories;

import com.example.fyp.R;
import com.example.fyp.models.Destination;
import com.example.fyp.models.Site;
import com.example.fyp.models.Tip;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class SiteCatalog {

    public static final String ALL_SITES = "All Sites";
    public static final String ROYAL_BELUM = "royal_belum";
    public static final String GUA_TEMPURUNG = "gua_tempurung";
    public static final String KUALA_SEPETANG = "kuala_sepetang";
    private static final String QR_PREFIX = "PERAK_ECO::";

    private static final Map<String, SiteMeta> SITE_MAP = new LinkedHashMap<>();

    static {
        add(new SiteMeta(
                ROYAL_BELUM,
                "Royal Belum State Park",
                "Ancient rainforest journeys with strict low-impact travel practices.",
                "Royal Belum is a biodiversity-rich rainforest destination known for hornbills, rafflesia blooms, and indigenous cultural heritage. Visitors should keep their footprint light because sensitive habitats and protected zones are easily disturbed.",
                "Hornbill sightings, rafflesia trails, and Orang Asli community experiences.",
                "Do: stay on marked trails, bring refillable bottles, respect permit rules, and follow ranger instructions.\nDo not: feed wildlife, enter restricted core zones, or bring single-use plastics.",
                "Protected breeding zones require permits and some trails may close during heavy rain.",
                5.9466,
                101.3594,
                200,
                54,
                "Rainforest wildlife, birdwatching, and conservation learning",
                "Dry months offer easier trekking. Wet periods require extra trail caution.",
                "Carry reusable gear and choose eco-lodges near the park gateway.",
                R.drawable.royal_belum
        ));

        add(new SiteMeta(
                GUA_TEMPURUNG,
                "Gua Tempurung",
                "Cave exploration with fragile limestone formations and guided adventure routes.",
                "Gua Tempurung is one of Peninsular Malaysia's best known cave systems. The site combines adventure tourism with delicate geological features that need careful visitor behavior, especially on wet routes.",
                "Limestone chambers, underground streams, and guided wet or dry cave tours.",
                "Do: wear proper grip shoes, listen to guides, and keep helmets and lamps ready.\nDo not: touch cave formations, shout at wildlife, or leave food waste inside the cave.",
                "Sections may close during heavy rain and slippery routes are not suitable for all visitors.",
                4.4148,
                101.1878,
                150,
                88,
                "Adventure caving, geology, and short half-day eco trips",
                "Morning visits are cooler and safer in the rainy season. Wet tours depend on current conditions.",
                "Combine cave trips with shared transport to reduce emissions from short-haul car travel.",
                R.drawable.gua_tempurung
        ));

        add(new SiteMeta(
                KUALA_SEPETANG,
                "Kuala Sepetang",
                "Mangrove, firefly, and community-based ecotourism around the Matang reserve.",
                "Kuala Sepetang blends mangrove ecology, firefly watching, charcoal kiln heritage, and community tourism. The site is especially sensitive to litter, boat disturbance, and disruptive lighting during evening tours.",
                "Mangrove reserves, firefly cruises, charcoal kilns, and seafood village culture.",
                "Do: use low-light settings, support local guides, and keep waterways free from waste.\nDo not: use flash during firefly tours, throw anything into the river, or disturb mangrove roots.",
                "Evening weather and tide conditions affect boat routes and boardwalk safety.",
                4.8371,
                100.6267,
                160,
                72,
                "Mangrove learning, birdwatching, and calm community-focused visits",
                "Dry evenings are best for firefly viewing. Monsoon tides may alter boat availability.",
                "Choose local vegetarian or seafood meals sourced from responsible community operators.",
                R.drawable.kuala_sepetang
        ));
    }

    private SiteCatalog() {
    }

    private static void add(SiteMeta meta) {
        SITE_MAP.put(meta.getId(), meta);
    }

    public static List<SiteMeta> getSiteMetas() {
        return new ArrayList<>(SITE_MAP.values());
    }

    public static SiteMeta getSiteMetaById(String siteId) {
        if (siteId == null) {
            return null;
        }
        return SITE_MAP.get(normalizeSiteId(siteId));
    }

    public static SiteMeta getSiteMetaByName(String name) {
        if (name == null) {
            return null;
        }
        for (SiteMeta meta : SITE_MAP.values()) {
            if (meta.getName().equalsIgnoreCase(name.trim())) {
                return meta;
            }
        }
        return getSiteMetaById(name);
    }

    public static String getSiteName(String siteId) {
        SiteMeta meta = getSiteMetaById(siteId);
        return meta != null ? meta.getName() : "Unknown Site";
    }

    public static String getSiteId(String siteName) {
        SiteMeta meta = getSiteMetaByName(siteName);
        return meta != null ? meta.getId() : "";
    }

    public static String normalizeSiteId(String raw) {
        if (raw == null) {
            return "";
        }

        String normalized = raw.trim().toLowerCase(Locale.US)
                .replace("state park", "")
                .replace("mangrove", "")
                .replace("  ", " ")
                .replace("-", "_")
                .replace(" ", "_");

        if (normalized.contains("royal_belum")) {
            return ROYAL_BELUM;
        }
        if (normalized.contains("gua_tempurung")) {
            return GUA_TEMPURUNG;
        }
        if (normalized.contains("kuala_sepetang")) {
            return KUALA_SEPETANG;
        }
        return normalized;
    }

    public static List<String> getSiteNames(boolean includeAllSites) {
        List<String> names = new ArrayList<>();
        if (includeAllSites) {
            names.add(ALL_SITES);
        }
        for (SiteMeta meta : SITE_MAP.values()) {
            names.add(meta.getName());
        }
        return names;
    }

    public static String buildQrPayload(String siteId) {
        return QR_PREFIX + normalizeSiteId(siteId);
    }

    public static String getSiteIdFromQrPayload(String payload) {
        if (payload == null) {
            return "";
        }
        String trimmed = payload.trim();
        if (trimmed.startsWith(QR_PREFIX)) {
            return normalizeSiteId(trimmed.substring(QR_PREFIX.length()));
        }
        return getSiteId(trimmed);
    }

    public static List<Site> getGuideSites() {
        List<Site> sites = new ArrayList<>();
        for (SiteMeta meta : SITE_MAP.values()) {
            sites.add(new Site(
                    meta.getName(),
                    meta.getShortDescription(),
                    meta.getLongDescription(),
                    meta.getHighlights(),
                    meta.getGuidelines(),
                    meta.getWarnings(),
                    meta.getImageResId(),
                    meta.getLatitude(),
                    meta.getLongitude()
            ));
        }
        return sites;
    }

    public static List<Destination> getDestinations(Map<String, Integer> liveVisitorCounts) {
        List<Destination> destinations = new ArrayList<>();
        for (SiteMeta meta : SITE_MAP.values()) {
            int currentVisitors = liveVisitorCounts != null && liveVisitorCounts.containsKey(meta.getId())
                    ? liveVisitorCounts.get(meta.getId())
                    : meta.getDefaultVisitorCount();
            destinations.add(new Destination(
                    meta.getName(),
                    meta.getLatitude(),
                    meta.getLongitude(),
                    meta.getShortDescription(),
                    meta.getHighlights(),
                    currentVisitors,
                    meta.getMaxCapacity()
            ));
        }
        return destinations;
    }

    public static List<Tip> getSiteSpecificTips() {
        List<Tip> tips = new ArrayList<>();

        tips.add(new Tip("rb_do_refill", "Bring refillables", "Bring refillables", "Use refillable bottles and food containers before entering Royal Belum to avoid plastic waste in fragile rainforest zones.", "Waste Reduction", "royal_belum", "Royal Belum State Park", 0.5));
        tips.add(new Tip("rb_do_trails", "Stay on ranger trails", "Stay on ranger trails", "Follow designated jungle routes in Royal Belum to protect undergrowth and reduce wildlife disturbance.", "Wildlife Care", "royal_belum", "Royal Belum State Park", 0.0));
        tips.add(new Tip("rb_dont_feed", "Do not feed wildlife", "Do not feed wildlife", "Keep hornbills, elephants, and other wildlife wild by never offering food or bait in Royal Belum.", "Wildlife Care", "royal_belum", "Royal Belum State Park", 0.0));
        tips.add(new Tip("rb_dont_core", "Avoid restricted core zones", "Avoid restricted core zones", "Protected areas inside Royal Belum require permits and guide approval. Respect all warning signs and ranger barriers.", "Community Respect", "royal_belum", "Royal Belum State Park", 0.0));

        tips.add(new Tip("gt_do_shoes", "Wear proper grip shoes", "Wear proper grip shoes", "Gua Tempurung routes can be slippery, especially in wet season. Good footwear prevents erosion and rescue incidents.", "Community Respect", "gua_tempurung", "Gua Tempurung", 0.0));
        tips.add(new Tip("gt_do_group", "Travel with shared transport", "Travel with shared transport", "Carpool or take a bus when visiting Gua Tempurung to reduce emissions for a short day trip.", "Waste Reduction", "gua_tempurung", "Gua Tempurung", 0.4));
        tips.add(new Tip("gt_dont_touch", "Do not touch formations", "Do not touch formations", "Skin oils damage cave formations and can slow natural mineral growth inside Gua Tempurung.", "Wildlife Care", "gua_tempurung", "Gua Tempurung", 0.0));
        tips.add(new Tip("gt_dont_noise", "Keep noise low", "Keep noise low", "Avoid shouting inside the cave because sound echoes strongly and can disturb cave fauna and other visitors.", "Community Respect", "gua_tempurung", "Gua Tempurung", 0.0));

        tips.add(new Tip("ks_do_flashless", "Use low-light photography", "Use low-light photography", "At Kuala Sepetang, low-light settings protect fireflies better than flash photography during night tours.", "Wildlife Care", "kuala_sepetang", "Kuala Sepetang", 0.0));
        tips.add(new Tip("ks_do_local", "Support local eco operators", "Support local eco operators", "Choose local boatmen, guides, and community meals in Kuala Sepetang to keep tourism benefits within the area.", "Community Respect", "kuala_sepetang", "Kuala Sepetang", 0.0));
        tips.add(new Tip("ks_dont_litter", "Keep waterways clean", "Keep waterways clean", "Never throw waste into mangrove channels or boardwalk areas in Kuala Sepetang.", "Waste Reduction", "kuala_sepetang", "Kuala Sepetang", 0.3));
        tips.add(new Tip("ks_dont_roots", "Do not step on mangrove roots", "Do not step on mangrove roots", "Mangrove roots stabilize the ecosystem, so remain on approved paths and jetty access points.", "Wildlife Care", "kuala_sepetang", "Kuala Sepetang", 0.0));

        return tips;
    }

    public static String buildFallbackPlan(SiteMeta meta, String interest, String availableTime, String season, int visitorCount) {
        String timeAdvice = "half-day";
        String crowdAdvice = visitorCount >= (int) (meta.getMaxCapacity() * 0.8)
                ? "The site is getting busy, so start early or use quieter trail segments."
                : "Crowd levels look manageable for a relaxed visit.";

        if (availableTime != null && availableTime.toLowerCase(Locale.US).contains("full")) {
            timeAdvice = "full-day";
        } else if (availableTime != null && availableTime.toLowerCase(Locale.US).contains("2")) {
            timeAdvice = "short 2-3 hour";
        }

        return meta.getName() + " fits a " + timeAdvice + " eco-itinerary focused on " + interest + ". "
                + crowdAdvice + " Best seasonal note: " + meta.getSeasonalAdvice() + " Suggested focus: "
                + meta.getBestFor() + ". Carbon tip: " + meta.getEcoTrackAdvice();
    }

    public static final class SiteMeta {
        private final String id;
        private final String name;
        private final String shortDescription;
        private final String longDescription;
        private final String highlights;
        private final String guidelines;
        private final String warnings;
        private final double latitude;
        private final double longitude;
        private final int maxCapacity;
        private final int defaultVisitorCount;
        private final String bestFor;
        private final String seasonalAdvice;
        private final String ecoTrackAdvice;
        private final int imageResId;

        public SiteMeta(String id,
                        String name,
                        String shortDescription,
                        String longDescription,
                        String highlights,
                        String guidelines,
                        String warnings,
                        double latitude,
                        double longitude,
                        int maxCapacity,
                        int defaultVisitorCount,
                        String bestFor,
                        String seasonalAdvice,
                        String ecoTrackAdvice,
                        int imageResId) {
            this.id = id;
            this.name = name;
            this.shortDescription = shortDescription;
            this.longDescription = longDescription;
            this.highlights = highlights;
            this.guidelines = guidelines;
            this.warnings = warnings;
            this.latitude = latitude;
            this.longitude = longitude;
            this.maxCapacity = maxCapacity;
            this.defaultVisitorCount = defaultVisitorCount;
            this.bestFor = bestFor;
            this.seasonalAdvice = seasonalAdvice;
            this.ecoTrackAdvice = ecoTrackAdvice;
            this.imageResId = imageResId;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getShortDescription() {
            return shortDescription;
        }

        public String getLongDescription() {
            return longDescription;
        }

        public String getHighlights() {
            return highlights;
        }

        public String getGuidelines() {
            return guidelines;
        }

        public String getWarnings() {
            return warnings;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public int getMaxCapacity() {
            return maxCapacity;
        }

        public int getDefaultVisitorCount() {
            return defaultVisitorCount;
        }

        public String getBestFor() {
            return bestFor;
        }

        public String getSeasonalAdvice() {
            return seasonalAdvice;
        }

        public String getEcoTrackAdvice() {
            return ecoTrackAdvice;
        }

        public int getImageResId() {
            return imageResId;
        }
    }
}
