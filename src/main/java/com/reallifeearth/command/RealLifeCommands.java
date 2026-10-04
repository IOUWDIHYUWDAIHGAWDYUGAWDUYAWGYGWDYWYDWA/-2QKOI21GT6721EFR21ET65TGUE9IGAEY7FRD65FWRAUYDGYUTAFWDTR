package com.reallifeearth.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.reallifeearth.osm.OsmCacheManager;
import com.reallifeearth.osm.OsmToMcConverter;
import com.reallifeearth.osm.OverpassFetcher;
import com.reallifeearth.entity.npc.NpcSpawner;
import com.reallifeearth.worldgen.BuildingPlacer;
import com.reallifeearth.worldgen.RealEarthData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class RealLifeCommands {

    @SubscribeEvent
    public static void register(RegisterCommandsEvent e) {
        var d = e.getDispatcher();
        d.register(Commands.literal("reallife")
                .then(Commands.literal("spawninfo").executes(RealLifeCommands::spawnInfo))
                .then(Commands.literal("spawnnpc")
                        .then(Commands.argument("count", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 50))
                                .executes(ctx -> {
                                    int c = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(ctx, "count");
                                    var src = ctx.getSource();
                                    var p = src.getPlayerOrException();
                                    int n = NpcSpawner.spawnNear(src.getLevel(), p.blockPosition(), c);
                                    src.sendSuccess(() -> net.minecraft.network.chat.Component.literal(n + " NPC spawn edildi"), true);
                                    return n;
                                })))
                .then(Commands.literal("buildcity")
                        .then(Commands.argument("lat", DoubleArgumentType.doubleArg(-90, 90))
                        .then(Commands.argument("lon", DoubleArgumentType.doubleArg(-180, 180))
                        .then(Commands.argument("radiusKm", DoubleArgumentType.doubleArg(0.2, 5))
                                .executes(RealLifeCommands::buildCity)))))
                .then(Commands.literal("buildhere")
                        .then(Commands.argument("radiusKm", DoubleArgumentType.doubleArg(0.2, 5))
                                .executes(ctx -> {
                                    var src = ctx.getSource();
                                    var p = src.getPlayerOrException();
                                    double lat, lon;
                                    // Use pending spawn if exists else default to Istanbul bbox
                                    if (!Double.isNaN(RealEarthData.pendingLat)) { lat = RealEarthData.pendingLat; lon = RealEarthData.pendingLon; }
                                    else if (p.getPersistentData().contains(RealEarthData.TAG_SPAWN_LAT)) {
                                        lat = p.getPersistentData().getDouble(RealEarthData.TAG_SPAWN_LAT);
                                        lon = p.getPersistentData().getDouble(RealEarthData.TAG_SPAWN_LON);
                                    } else { lat = 41.0082; lon = 28.9784; }
                                    double r = DoubleArgumentType.getDouble(ctx, "radiusKm");
                                    return buildCityInternal(ctx, lat, lon, r);
                                })))
                .then(Commands.literal("search")
                        .then(Commands.argument("query", StringArgumentType.greedyString())
                                .executes(RealLifeCommands::search)))
        );
    }

    private static int spawnInfo(CommandContext<CommandSourceStack> ctx) {
        var src = ctx.getSource();
        var p = src.getPlayer();
        double lat = Double.NaN, lon = Double.NaN; String label = "";
        if (p != null) {
            var tag = p.getPersistentData();
            if (tag.contains(RealEarthData.TAG_SPAWN_LAT)) {
                lat = tag.getDouble(RealEarthData.TAG_SPAWN_LAT);
                lon = tag.getDouble(RealEarthData.TAG_SPAWN_LON);
                label = tag.getString(RealEarthData.TAG_SPAWN_LABEL);
            }
        }
        if (Double.isNaN(lat) && !Double.isNaN(RealEarthData.pendingLat)) {
            lat = RealEarthData.pendingLat; lon = RealEarthData.pendingLon; label = RealEarthData.pendingLabel;
        }
        if (Double.isNaN(lat)) src.sendSuccess(() -> Component.literal("Spawn henuz secilmedi. /reallife buildcity <lat> <lon> <km> kullan."), false);
        else { String fLabel = label; double fLat = lat, fLon = lon; src.sendSuccess(() -> Component.literal(String.format("Spawn: %s (%.5f, %.5f)", fLabel, fLat, fLon)), false); }
        return 1;
    }

    private static int buildCity(CommandContext<CommandSourceStack> ctx) {
        double lat = DoubleArgumentType.getDouble(ctx, "lat");
        double lon = DoubleArgumentType.getDouble(ctx, "lon");
        double r = DoubleArgumentType.getDouble(ctx, "radiusKm");
        return buildCityInternal(ctx, lat, lon, r);
    }

    private static int buildCityInternal(CommandContext<CommandSourceStack> ctx, double lat, double lon, double radiusKm) {
        var src = ctx.getSource();
        src.sendSuccess(() -> Component.literal(String.format("Sehir yukleniyor: %.4f, %.4f (%.1f km) ...", lat, lon, radiusKm)), true);
        double deg = radiusKm / 111.0;
        double s = lat - deg, n = lat + deg, w = lon - deg, e = lon + deg;
        ServerLevel level = src.getLevel();
        BlockPos origin = BlockPos.containing(src.getPosition());
        // Try cache first
        var cache = OsmCacheManager.load(level.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).resolve("data").getParent(), lat, lon, deg);
        // We'll just fetch
        OverpassFetcher.fetch(s, w, n, e).whenComplete((json, ex) -> {
            if (ex != null || json == null) {
                src.sendFailure(Component.literal("Overpass hatasi: " + (ex != null ? ex.getMessage() : "null")));
                return;
            }
            level.getServer().execute(() -> {
                try {
                    String country = OsmToMcConverter.detectCountry(json);
                    var buildings = OsmToMcConverter.parseBuildings(json, lat, lon, country);
                    var roads = OsmToMcConverter.parseRoads(json, lat, lon);
                    int bc = BuildingPlacer.placeBuildings(level, buildings, origin);
                    int rc = BuildingPlacer.placeRoads(level, roads, origin);
                    src.sendSuccess(() -> Component.literal(String.format("Bitti! Bina: %d, Yol: %d (%.1f km cap)", bc, rc, radiusKm)), true);
                    // Save cache
                    try { OsmCacheManager.save(level.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).resolve("data").getParent(), lat, lon, deg, json); } catch (Exception ignored) {}
                } catch (Exception ee) {
                    src.sendFailure(Component.literal("Yerlestirme hatasi: " + ee.getMessage()));
                }
            });
        });
        return 1;
    }

    private static int search(CommandContext<CommandSourceStack> ctx) {
        String q = StringArgumentType.getString(ctx, "query");
        var src = ctx.getSource();
        src.sendSuccess(() -> Component.literal("Araniyor: " + q), false);
        OverpassFetcher.nominatimSearch(q).whenComplete((wrapper, ex) -> {
            src.getServer().execute(() -> {
                if (ex != null || wrapper == null) {
                    src.sendFailure(Component.literal("Arama hatasi"));
                    return;
                }
                try {
                    var arr = wrapper.getAsJsonArray("results");
                    if (arr == null || arr.isEmpty()) { src.sendFailure(Component.literal("Sonuc yok")); return; }
                    for (int j = 0; j < Math.min(5, arr.size()); j++) {
                        var o = arr.get(j).getAsJsonObject();
                        double la = o.get("lat").getAsDouble(), lo = o.get("lon").getAsDouble();
                        String name = o.has("display_name") ? o.get("display_name").getAsString() : q;
                        double finLa = la, finLo = lo; String finName = name; int idx = j+1;
                        src.sendSuccess(() -> Component.literal(String.format("%d) %s (%.4f, %.4f) - /reallife buildcity %.4f %.4f 1", idx, finName, finLa, finLo, finLa, finLo)), false);
                    }
                } catch (Exception e) { src.sendFailure(Component.literal("Parse hatasi: " + e.getMessage())); }
            });
        });
        return 1;
    }
}
