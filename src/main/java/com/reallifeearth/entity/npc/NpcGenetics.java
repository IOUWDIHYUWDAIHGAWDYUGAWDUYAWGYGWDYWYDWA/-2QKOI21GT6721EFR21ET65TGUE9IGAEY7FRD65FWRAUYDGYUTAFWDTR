package com.reallifeearth.entity.npc;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * Country-based height distribution + skin variants.
 * Loaded from data/reallifeearth/genetics/countries.json (future: reload listener)
 */
public class NpcGenetics {
    private static final Logger LOG = LogUtils.getLogger();

    public record CountryData(float maleAvgCm, float femaleAvgCm, List<String> maleSkins, List<String> femaleSkins,
                              String language, String currency) {}

    private static final Map<String, CountryData> COUNTRIES = new HashMap<>();

    private static void loadDefaults() {
        COUNTRIES.put("TR", new CountryData(176, 162, List.of(), List.of(), "tr", "TRY"));
        COUNTRIES.put("US", new CountryData(177, 163, List.of(), List.of(), "en", "USD"));
        COUNTRIES.put("DE", new CountryData(180, 167, List.of(), List.of(), "de", "EUR"));
        COUNTRIES.put("JP", new CountryData(171, 158, List.of(), List.of(), "jp", "JPY"));
        COUNTRIES.put("BR", new CountryData(175, 162, List.of(), List.of(), "pt", "BRL"));
        COUNTRIES.put("GB", new CountryData(178, 164, List.of(), List.of(), "en", "GBP"));
        COUNTRIES.put("FR", new CountryData(179, 164, List.of(), List.of(), "fr", "EUR"));
        COUNTRIES.put("IN", new CountryData(166, 152, List.of(), List.of(), "hi", "INR"));
        COUNTRIES.put("CN", new CountryData(172, 160, List.of(), List.of(), "zh", "CNY"));
        COUNTRIES.put("RU", new CountryData(178, 166, List.of(), List.of(), "ru", "RUB"));
        COUNTRIES.put("DEFAULT", new CountryData(175, 162, List.of(), List.of(), "en", "USD"));
    }

    public static CountryData get(String code) {
        if (COUNTRIES.isEmpty()) {
            loadDefaults();
        }
        return COUNTRIES.getOrDefault(code, COUNTRIES.get("DEFAULT"));
    }

    /** height scale 0.92-1.08, country avg maps to 1.0 */
    public static float heightScale(String country, boolean male, net.minecraft.util.RandomSource rand) {
        var d = get(country);
        float avg = male ? d.maleAvgCm() : d.femaleAvgCm();
        // gaussian around avg, map to scale
        float offset = (rand.nextFloat() - 0.5f) * 14f; // +-7cm
        float cm = avg + offset;
        return cm / avg; // ~0.96 - 1.04
    }
}
