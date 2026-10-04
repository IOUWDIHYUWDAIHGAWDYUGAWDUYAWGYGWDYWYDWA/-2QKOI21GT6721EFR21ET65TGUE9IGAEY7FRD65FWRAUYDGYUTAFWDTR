package com.reallifeearth.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class RealLifeConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec CLIENT_SPEC;

    // Common
    public static final ModConfigSpec.BooleanValue ENABLE_NEEDS;
    public static final ModConfigSpec.BooleanValue ENABLE_ECONOMY;
    public static final ModConfigSpec.BooleanValue ENABLE_POLICE;
    public static final ModConfigSpec.IntValue CITY_RADIUS_BLOCKS;
    public static final ModConfigSpec.IntValue NPCS_PER_BUILDING;
    public static final ModConfigSpec.DoubleValue NPC_TICK_DISTANCE;

    // Client
    public static final ModConfigSpec.BooleanValue MODERN_GUI;
    public static final ModConfigSpec.BooleanValue MAP_OFFLINE_CACHE;
    public static final ModConfigSpec.IntValue MAP_TILE_CACHE_MB;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.push("gameplay");
        ENABLE_NEEDS = b.comment("Susuzluk/Enerji/Hijyen sistemleri aktif olsun mu").define("enableNeeds", true);
        ENABLE_ECONOMY = b.comment("Para/Banka/Kira sistemi aktif olsun mu").define("enableEconomy", true);
        ENABLE_POLICE = b.comment("Polis/Suc sistemi aktif olsun mu").define("enablePolice", true);
        CITY_RADIUS_BLOCKS = b.comment("OSM sehir yari capi (blok)").defineInRange("cityRadiusBlocks", 2500, 500, 10000);
        NPCS_PER_BUILDING = b.comment("Konut binasi basina max NPC").defineInRange("npcsPerBuilding", 6, 1, 20);
        NPC_TICK_DISTANCE = b.comment("NPC AI tick mesafesi (blok)").defineInRange("npcTickDistance", 64.0, 16.0, 256.0);
        b.pop();
        SPEC = b.build();

        ModConfigSpec.Builder cb = new ModConfigSpec.Builder();
        cb.push("client");
        MODERN_GUI = cb.comment("Modern yuvarlak GUI kullan").define("modernGui", true);
        MAP_OFFLINE_CACHE = cb.comment("Harita tile ve OSM verisini diske cachele").define("mapOfflineCache", true);
        MAP_TILE_CACHE_MB = cb.defineInRange("mapTileCacheMb", 512, 64, 4096);
        cb.pop();
        CLIENT_SPEC = cb.build();
    }
}
