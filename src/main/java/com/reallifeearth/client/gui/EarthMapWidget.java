package com.reallifeearth.client.gui;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import com.reallifeearth.osm.OverpassFetcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Tile-based OSM map widget. Downloads tiles via HttpClient, caches in memory (and later disk).
 * Supports pan (drag), zoom (wheel), click to select lat/lon, and Nominatim search.
 */
public class EarthMapWidget extends AbstractWidget {
    public record SearchResult(double lat, double lon, String label) {}

    @FunctionalInterface
    public interface SelectListener { void onSelect(double lat, double lon, String label); }

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .followRedirects(HttpClient.Redirect.NORMAL).build();

    private static final int TILE_SIZE = 256;
    private final Map<String, ResourceLocation> tileTextures = new ConcurrentHashMap<>();
    private final Map<String, byte[]> tileCacheBytes = new ConcurrentHashMap<>();

    private double centerLat, centerLon;
    private int zoom; // 2..16
    private double dragStartX, dragStartY;
    private double dragStartLat, dragStartLon;
    private boolean dragging = false;

    // Selected pin
    private double pinLat, pinLon;
    private boolean hasPin = true;

    private SelectListener onSelect;

    public EarthMapWidget(int x, int y, int w, int h, double lat, double lon, int zoom) {
        super(x, y, w, h, Component.empty());
        this.centerLat = lat; this.centerLon = lon; this.zoom = Math.clamp(zoom, 2, 16);
        this.pinLat = lat; this.pinLon = lon;
    }

    public void setOnSelect(SelectListener l) { this.onSelect = l; }

    public void flyTo(double lat, double lon, int z) {
        this.centerLat = lat; this.centerLon = lon; this.zoom = Math.clamp(z, 2, 16);
        this.pinLat = lat; this.pinLon = lon; this.hasPin = true;
    }

    public void search(String q, Consumer<SearchResult> cb) {
        OverpassFetcher.nominatimSearch(q).whenComplete((wrapper, ex) -> {
            if (ex != null || wrapper == null) {
                Minecraft.getInstance().execute(() -> cb.accept(null));
                return;
            }
            try {
                JsonArray arr = wrapper.getAsJsonArray("results");
                if (arr == null || arr.isEmpty()) {
                    Minecraft.getInstance().execute(() -> cb.accept(null));
                    return;
                }
                JsonObject first = arr.get(0).getAsJsonObject();
                double lat = first.get("lat").getAsDouble();
                double lon = first.get("lon").getAsDouble();
                String label = first.has("display_name") ? first.get("display_name").getAsString() : q;
                Minecraft.getInstance().execute(() -> cb.accept(new SearchResult(lat, lon, label)));
            } catch (Exception e) {
                Minecraft.getInstance().execute(() -> cb.accept(null));
            }
        });
    }

    // Mercator
    private static double lonToX(double lon, int zoom) {
        return (lon + 180.0) / 360.0 * (1 << zoom);
    }
    private static double latToY(double lat, int zoom) {
        double rad = Math.toRadians(lat);
        return (1 - Math.log(Math.tan(rad) + 1 / Math.cos(rad)) / Math.PI) / 2 * (1 << zoom);
    }
    private static double xToLon(double x, int zoom) {
        return x / (1 << zoom) * 360.0 - 180.0;
    }
    private static double yToLat(double y, int zoom) {
        double n = Math.PI - 2 * Math.PI * y / (1 << zoom);
        return Math.toDegrees(Math.atan(Math.sinh(n)));
    }

    private void fetchTile(int tx, int ty, int z) {
        String key = z + "/" + tx + "/" + ty;
        if (tileTextures.containsKey(key) || tileCacheBytes.containsKey(key + "_fetching")) return;
        tileCacheBytes.put(key + "_fetching", new byte[0]);
        String url = "https://tile.openstreetmap.org/" + z + "/" + tx + "/" + ty + ".png";
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("User-Agent", "RealLifeEarth/1.0")
                .GET().build();
        HTTP.sendAsync(req, HttpResponse.BodyHandlers.ofByteArray()).whenComplete((resp, ex) -> {
            tileCacheBytes.remove(key + "_fetching");
            if (ex != null || resp.statusCode() != 200) return;
            byte[] bytes = resp.body();
            tileCacheBytes.put(key, bytes);
            Minecraft.getInstance().execute(() -> uploadTile(key, bytes));
        });
    }

    private void uploadTile(String key, byte[] bytes) {
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
            if (img == null) return;
            int w = img.getWidth(), h = img.getHeight();
            int[] pixels = new int[w * h];
            img.getRGB(0, 0, w, h, pixels, 0, w);
            // Convert ARGB -> RGBA for DynamicTexture
            var tex = new DynamicTexture(w, h, false);
            var nativeImg = tex.getPixels();
            if (nativeImg == null) return;
            for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
                int argb = pixels[y * w + x];
                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                nativeImg.setPixelRGBA(x, y, (a << 24) | (b << 16) | (g << 8) | r);
            }
            tex.upload();
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath("reallifeearth", "tile_" + key.replace("/", "_"));
            Minecraft.getInstance().getTextureManager().register(id, tex);
            tileTextures.put(key, id);
        } catch (Exception ignored) {}
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mx, int my, float pt) {
        // Background
        g.fill(getX(), getY(), getX() + width, getY() + height, 0xFF1a2a3a);
        // Clip
        g.enableScissor(getX(), getY(), getX() + width, getY() + height);

        double centerX = lonToX(centerLon, zoom);
        double centerY = latToY(centerLat, zoom);
        double tilesX = (double) width / TILE_SIZE;
        double tilesY = (double) height / TILE_SIZE;

        int minTx = (int) Math.floor(centerX - tilesX / 2) - 1;
        int maxTx = (int) Math.ceil(centerX + tilesX / 2) + 1;
        int minTy = (int) Math.floor(centerY - tilesY / 2) - 1;
        int maxTy = (int) Math.ceil(centerY + tilesY / 2) + 1;
        int n = 1 << zoom;

        for (int ty = minTy; ty <= maxTy; ty++) {
            for (int tx = minTx; tx <= maxTx; tx++) {
                int wrappedTx = ((tx % n) + n) % n;
                if (ty < 0 || ty >= n) continue;
                String key = zoom + "/" + wrappedTx + "/" + ty;
                double tileScreenX = getX() + width / 2.0 + (tx - centerX) * TILE_SIZE;
                double tileScreenY = getY() + height / 2.0 + (ty - centerY) * TILE_SIZE;
                ResourceLocation tex = tileTextures.get(key);
                if (tex != null) {
                    g.blit(tex, (int) tileScreenX, (int) tileScreenY, 0, 0, TILE_SIZE, TILE_SIZE, TILE_SIZE, TILE_SIZE);
                } else {
                    // placeholder + trigger fetch
                    g.fill((int) tileScreenX, (int) tileScreenY, (int) tileScreenX + TILE_SIZE, (int) tileScreenY + TILE_SIZE, 0xFF223344);
                    fetchTile(wrappedTx, ty, zoom);
                }
            }
        }

        // Crosshair at center
        int cx = getX() + width / 2;
        int cy = getY() + height / 2;
        g.fill(cx - 10, cy, cx + 10, cy + 1, 0x88FFFFFF);
        g.fill(cx, cy - 10, cx + 1, cy + 10, 0x88FFFFFF);

        // Pin
        if (hasPin) {
            double pinX = lonToX(pinLon, zoom);
            double pinY = latToY(pinLat, zoom);
            int px = (int) (getX() + width / 2 + (pinX - centerX) * TILE_SIZE);
            int py = (int) (getY() + height / 2 + (pinY - centerY) * TILE_SIZE);
            // pin triangle + circle
            g.fill(px - 3, py - 14, px + 3, py - 2, 0xFFFF4444);
            g.fill(px - 1, py - 2, px + 1, py + 4, 0xFFFF4444);
            // dot
            g.fill(px - 2, py - 10, px + 2, py - 6, 0xFFFFFFFF);
        }

        // Border
        g.disableScissor();
        g.renderOutline(getX(), getY(), width, height, 0xFF3a4a5a);
        // Zoom label
        g.drawString(Minecraft.getInstance().font, "Zoom: " + zoom, getX() + 4, getY() + height - 12, 0xFFFFFF, true);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        if (btn == 0) {
            if (!dragging) {
                dragging = true;
                dragStartX = mx; dragStartY = my;
                dragStartLat = centerLat; dragStartLon = centerLon;
            }
            double dXtiles = (mx - dragStartX) / TILE_SIZE;
            double dYtiles = (my - dragStartY) / TILE_SIZE;
            double startX = lonToX(dragStartLon, zoom);
            double startY = latToY(dragStartLat, zoom);
            double newX = startX - dXtiles;
            double newY = startY - dYtiles;
            centerLon = xToLon(newX, zoom);
            centerLat = yToLat(newY, zoom);
            centerLat = Math.clamp(centerLat, -85, 85);
            return true;
        }
        return super.mouseDragged(mx, my, btn, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int btn) {
        dragging = false;
        return super.mouseReleased(mx, my, btn);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (btn == 0 && isMouseOver(mx, my)) {
            // Place pin at clicked location
            double centerX = lonToX(centerLon, zoom);
            double centerY = latToY(centerLat, zoom);
            double clickTileX = centerX + (mx - (getX() + width / 2.0)) / TILE_SIZE;
            double clickTileY = centerY + (my - (getY() + height / 2.0)) / TILE_SIZE;
            pinLon = xToLon(clickTileX, zoom);
            pinLat = yToLat(clickTileY, zoom);
            hasPin = true;
            if (onSelect != null) onSelect.onSelect(pinLat, pinLon, String.format("%.4f, %.4f", pinLat, pinLon));
            return true;
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        if (!isMouseOver(mx, my)) return false;
        int newZoom = Math.clamp(zoom + (sy > 0 ? 1 : -1), 2, 16);
        if (newZoom != zoom) {
            zoom = newZoom;
        }
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput out) {}
}
