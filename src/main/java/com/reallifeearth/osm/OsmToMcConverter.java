package com.reallifeearth.osm;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts OSM way geometry to Minecraft block positions.
 * 1 block ~= 2 meters. Origin lat/lon -> (0,0) in MC.
 */
public class OsmToMcConverter {
    public static final double METERS_PER_DEGREE_LAT = 111320.0;
    public static double metersPerDegreeLon(double lat) {
        return METERS_PER_DEGREE_LAT * Math.cos(Math.toRadians(lat));
    }

    public record Building(List<BlockPos> polygon, int levels, String type, String name) {}
    public record Road(List<BlockPos> points, String highwayType, int width) {}

    public static double lonToMcX(double lon, double originLon, double originLat) {
        return (lon - originLon) * metersPerDegreeLon(originLat) / 2.0;
    }
    public static double latToMcZ(double lat, double originLat) {
        return (originLat - lat) * METERS_PER_DEGREE_LAT / 2.0; // flip Z
    }

    public static List<Building> parseBuildings(JsonObject overpassJson, double originLat, double originLon, String country) {
        List<Building> out = new ArrayList<>();
        if (!overpassJson.has("elements")) return out;
        JsonArray elems = overpassJson.getAsJsonArray("elements");
        for (JsonElement el : elems) {
            JsonObject o = el.getAsJsonObject();
            if (!o.has("tags")) continue;
            JsonObject tags = o.getAsJsonObject("tags");
            if (!tags.has("building")) continue;
            if (!o.has("geometry")) continue;
            JsonArray geom = o.getAsJsonArray("geometry");
            List<BlockPos> poly = new ArrayList<>();
            for (JsonElement g : geom) {
                JsonObject p = g.getAsJsonObject();
                double lat = p.get("lat").getAsDouble();
                double lon = p.get("lon").getAsDouble();
                int x = (int) Math.round(lonToMcX(lon, originLon, originLat));
                int z = (int) Math.round(latToMcZ(lat, originLat));
                poly.add(new BlockPos(x, 0, z));
            }
            if (poly.size() < 3) continue;
            String bType = tags.has("building") ? tags.get("building").getAsString() : "yes";
            String name = tags.has("name") ? tags.get("name").getAsString() : "";
            int levels = parseLevels(tags, country);
            out.add(new Building(poly, levels, bType, name));
        }
        return out;
    }

    public static List<Road> parseRoads(JsonObject overpassJson, double originLat, double originLon) {
        List<Road> out = new ArrayList<>();
        if (!overpassJson.has("elements")) return out;
        JsonArray elems = overpassJson.getAsJsonArray("elements");
        for (JsonElement el : elems) {
            JsonObject o = el.getAsJsonObject();
            if (!o.has("tags")) continue;
            JsonObject tags = o.getAsJsonObject("tags");
            if (!tags.has("highway")) continue;
            if (!o.has("geometry")) continue;
            JsonArray geom = o.getAsJsonArray("geometry");
            List<BlockPos> pts = new ArrayList<>();
            for (JsonElement g : geom) {
                JsonObject p = g.getAsJsonObject();
                double lat = p.get("lat").getAsDouble();
                double lon = p.get("lon").getAsDouble();
                int x = (int) Math.round(lonToMcX(lon, originLon, originLat));
                int z = (int) Math.round(latToMcZ(lat, originLat));
                pts.add(new BlockPos(x, 0, z));
            }
            if (pts.size() < 2) continue;
            String hw = tags.get("highway").getAsString();
            int w = roadWidth(hw);
            out.add(new Road(pts, hw, w));
        }
        return out;
    }

    public static String detectCountry(JsonObject overpassJson) {
        // Try to infer from addresses in tags, fallback TR
        if (overpassJson.has("elements")) {
            for (var el : overpassJson.getAsJsonArray("elements")) {
                var o = el.getAsJsonObject();
                if (!o.has("tags")) continue;
                var tags = o.getAsJsonObject("tags");
                if (tags.has("addr:country")) {
                    String c = tags.get("addr:country").getAsString().toUpperCase();
                    if (c.length() == 2) return c;
                }
            }
        }
        return "TR";
    }

    private static int parseLevels(JsonObject tags, String country) {
        if (tags.has("building:levels")) {
            try { return Math.max(1, tags.get("building:levels").getAsInt()); } catch (Exception ignored) {}
        }
        if (tags.has("height")) {
            try {
                String h = tags.get("height").getAsString().replace("m","").trim();
                double meters = Double.parseDouble(h);
                return Math.max(1, (int)Math.round(meters / 3.0));
            } catch (Exception ignored) {}
        }
        // country defaults
        return switch (country) {
            case "JP" -> 4 + (int)(Math.random()*8);
            case "US" -> 2 + (int)(Math.random()*3);
            case "TR" -> 3 + (int)(Math.random()*6);
            case "DE","FR","GB" -> 3 + (int)(Math.random()*4);
            default -> 2 + (int)(Math.random()*4);
        };
    }

    private static int roadWidth(String hw) {
        return switch (hw) {
            case "motorway","trunk" -> 7;
            case "primary","secondary" -> 5;
            case "tertiary","residential" -> 4;
            case "footway","path","steps" -> 2;
            default -> 3;
        };
    }
}
