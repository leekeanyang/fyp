package com.example.fyp.repositories;

import com.example.fyp.models.Tip;

import java.util.ArrayList;
import java.util.List;

public final class TipRepository {

    private TipRepository() {
    }

    public static List<Tip> getStaticTips() {
        return new ArrayList<>(SiteCatalog.getSiteSpecificTips());
    }
}
