package com.reallifeearth.osm;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class OsmCacheManager {
    private static final Logger LOG = LogUtils.getLogger();

    public static Path cacheDir(Path worldDataDir) {
        return worldDataDir.resolve("reallifeearth").resolve("osm_cache");
    }

    public static String keyFor(double lat, double lon, double radiusDeg) {
        return String.format("%.4f_%.4f_%.3f", lat, lon, radiusDeg);
    }

    public static Path fileFor(Path worldDataDir, double lat, double lon, double radiusDeg) {
        return cacheDir(worldDataDir).resolve(keyFor(lat, lon, radiusDeg) + ".json");
    }

    public static JsonObject load(Path worldDataDir, double lat, double lon, double radiusDeg) {
        Path f = fileFor(worldDataDir, lat, lon, radiusDeg);
        if (!Files.exists(f)) return null;
        try {
            String s = Files.readString(f);
            return JsonParser.parseString(s).getAsJsonObject();
        } catch (Exception e) {
            LOG.warn("[RLE] Cache load fail {}", f, e);
            return null;
        }
    }

    public static void save(Path worldDataDir, double lat, double lon, double radiusDeg, JsonObject data) {
        try {
            Path dir = cacheDir(worldDataDir);
            Files.createDirectories(dir);
            Path f = fileFor(worldDataDir, lat, lon, radiusDeg);
            Files.writeString(f, data.toString());
        } catch (IOException e) {
            LOG.warn("[RLE] Cache save fail", e);
        }
    }
}
