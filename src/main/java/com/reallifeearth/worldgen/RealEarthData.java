package com.reallifeearth.worldgen;

import com.reallifeearth.RealLifeEarthMod;
import com.reallifeearth.osm.OsmToMcConverter;
import com.reallifeearth.osm.OverpassFetcher;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Secilen spawn bilgisini world creation sonrasi oyuncuya uygular.
 * Client screen tarafindan System property / level.dat custom tag ile tasinir.
 * Ilk login'de isinlama + spawn set.
 */
public class RealEarthData {
    public static final String TAG_SPAWN_LAT = "reallifeearth:spawn_lat";
    public static final String TAG_SPAWN_LON = "reallifeearth:spawn_lon";
    public static final String TAG_SPAWN_LABEL = "reallifeearth:spawn_label";

    // Client tarafindan set edilir (RealEarthCreationScreen)
    public static volatile double pendingLat = Double.NaN;
    public static volatile double pendingLon = Double.NaN;
    public static volatile String pendingLabel = "";

    public static boolean hasPendingSpawn() {
        return !Double.isNaN(pendingLat) && !Double.isNaN(pendingLon);
    }

    public static void clearPending() {
        pendingLat = Double.NaN;
        pendingLon = Double.NaN;
        pendingLabel = "";
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        var tag = sp.getPersistentData();
        if (tag.getBoolean("rle_spawn_done")) return;

        if (!hasPendingSpawn()) {
            // Try read from player tag (reload case)
            if (tag.contains(TAG_SPAWN_LAT)) return;
            return;
        }

        double lat = pendingLat;
        double lon = pendingLon;
        String label = pendingLabel;
        ServerLevel level = sp.serverLevel();

        var spawn = level.getSharedSpawnPos();
        sp.teleportTo(spawn.getX() + 0.5, level.getSeaLevel() + 2, spawn.getZ() + 0.5);
        level.setDefaultSpawnPos(spawn, 0);
        tag.putBoolean("rle_spawn_done", true);
        tag.putDouble(TAG_SPAWN_LAT, lat);
        tag.putDouble(TAG_SPAWN_LON, lon);
        if (!label.isEmpty()) tag.putString(TAG_SPAWN_LABEL, label);

        RealLifeEarthMod.LOGGER.info("[RealLifeEarth] Spawn set: {} ({},{})", label, lat, lon);
        sp.sendSystemMessage(Component.literal("§a[RealLifeEarth] §fKonum: " + (label.isEmpty() ? String.format("%.4f, %.4f", lat, lon) : label)));
        sp.sendSystemMessage(Component.literal("§e[RealLifeEarth] §7Sehir OSM'den yukleniyor... (1-2 dk surebilir)"));

        double radiusKm = 0.8;
        double deg = radiusKm / 111.0;
        double s = lat - deg, n = lat + deg, w = lon - deg, e = lon + deg;
        BlockPos origin = spawn;
        ServerLevel sl = level;

        OverpassFetcher.fetch(s, w, n, e).whenComplete((json, ex) -> {
            if (ex != null || json == null) {
                RealLifeEarthMod.LOGGER.warn("[RealLifeEarth] Auto-build OSM fetch failed", ex);
                sl.getServer().execute(() -> sp.sendSystemMessage(Component.literal("§c[RealLifeEarth] OSM yuklenemedi. Komutla dene: /reallife buildcity " + String.format("%.4f %.4f 1", lat, lon))));
                return;
            }
            sl.getServer().execute(() -> {
                try {
                    String country = OsmToMcConverter.detectCountry(json);
                    var buildings = OsmToMcConverter.parseBuildings(json, lat, lon, country);
                    var roads = OsmToMcConverter.parseRoads(json, lat, lon);
                    RealLifeEarthMod.LOGGER.info("[RealLifeEarth] Auto-build parsed: {} bina, {} yol", buildings.size(), roads.size());
                    if (buildings.isEmpty() && roads.isEmpty()) {
                        sp.sendSystemMessage(Component.literal("§e[RealLifeEarth] Bu bolgede OSM binasi bulunamadi. Baska bir sehir dene veya /reallife search <sehir>"));
                        return;
                    }
                    sp.sendSystemMessage(Component.literal(String.format("§a[RealLifeEarth] §f%d bina, %d yol yerlestiriliyor...", buildings.size(), roads.size())));
                    int bc = BuildingPlacer.placeBuildings(sl, buildings, origin);
                    int rc = BuildingPlacer.placeRoads(sl, roads, origin);
                    sp.sendSystemMessage(Component.literal(String.format("§a[RealLifeEarth] §fBitti! %d bina, %d yol yerlestirildi. §7(/reallife buildhere 2 ile genisletebilirsin)", bc, rc)));
                    RealLifeEarthMod.LOGGER.info("[RealLifeEarth] Auto-build done: {} bina, {} yol", bc, rc);
                    // spawn some NPCs around spawn
                    try {
                        int npcCount = Math.min(20, Math.max(6, bc / 6));
                        int spawned = com.reallifeearth.entity.npc.NpcSpawner.spawnNear(sl, origin, npcCount);
                        if (spawned > 0) sp.sendSystemMessage(Component.literal(String.format("§e[RealLifeEarth] §f%d NPC yerlestirildi!", spawned)));
                    } catch (Exception npcEx) {
                        RealLifeEarthMod.LOGGER.warn("[RealLifeEarth] NPC spawn failed", npcEx);
                    }
                } catch (Exception ee) {
                    RealLifeEarthMod.LOGGER.error("[RealLifeEarth] Auto-build place failed", ee);
                    sp.sendSystemMessage(Component.literal("§c[RealLifeEarth] Yerlestirme hatasi: " + ee.getMessage()));
                }
            });
        });

        clearPending();
    }
}
