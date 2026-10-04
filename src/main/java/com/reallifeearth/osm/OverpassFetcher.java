package com.reallifeearth.osm;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Async Overpass fetcher. No external deps - uses java.net.http.
 */
public class OverpassFetcher {
    private static final Logger LOG = LogUtils.getLogger();
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private static final String[] ENDPOINTS = {
            "https://overpass-api.de/api/interpreter",
            "https://overpass.kumi.systems/api/interpreter",
            "https://maps.mail.ru/osm/tools/overpass/api/interpreter"
    };

    public static CompletableFuture<JsonObject> fetch(double south, double west, double north, double east) {
        // bbox query: buildings + highways
        String query = "[out:json][timeout:25];(way[\"building\"](%f,%f,%f,%f);way[\"highway\"](%f,%f,%f,%f););out geom;".formatted(
                south, west, north, east, south, west, north, east);
        String body = "data=" + java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8);
        return tryEndpoint(0, body);
    }

    private static CompletableFuture<JsonObject> tryEndpoint(int idx, String body) {
        if (idx >= ENDPOINTS.length) {
            CompletableFuture<JsonObject> f = new CompletableFuture<>();
            f.completeExceptionally(new RuntimeException("All Overpass endpoints failed"));
            return f;
        }
        String url = ENDPOINTS[idx];
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("User-Agent", "RealLifeEarth/1.0 (NeoForge 1.21.1)")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return CLIENT.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenCompose(resp -> {
                    if (resp.statusCode() == 200) {
                        try {
                            JsonObject obj = JsonParser.parseString(resp.body()).getAsJsonObject();
                            return CompletableFuture.completedFuture(obj);
                        } catch (Exception e) {
                            LOG.warn("[RLE] Parse fail endpoint {}", url, e);
                            return tryEndpoint(idx + 1, body);
                        }
                    } else {
                        LOG.warn("[RLE] Overpass {} status {}", url, resp.statusCode());
                        return tryEndpoint(idx + 1, body);
                    }
                }).exceptionallyCompose(ex -> {
                    LOG.warn("[RLE] Overpass endpoint {} failed", url, ex);
                    return tryEndpoint(idx + 1, body);
                });
    }

    /** Nominatim search */
    public static CompletableFuture<JsonObject> nominatimSearch(String q) {
        String url = "https://nominatim.openstreetmap.org/search?q=" + java.net.URLEncoder.encode(q, java.nio.charset.StandardCharsets.UTF_8)
                + "&format=json&limit=8&addressdetails=1";
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("User-Agent", "RealLifeEarth/1.0 (NeoForge 1.21.1)")
                .GET().build();
        return CLIENT.sendAsync(req, HttpResponse.BodyHandlers.ofString()).thenApply(r -> {
            // wrap array into object {results: [...]}
            JsonObject wrapper = new JsonObject();
            wrapper.add("results", JsonParser.parseString(r.body()));
            return wrapper;
        });
    }
}
