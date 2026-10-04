package com.reallifeearth.client.gui;

import com.reallifeearth.worldgen.RealEarthData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Replaces vanilla CreateWorldScreen via ScreenEvent.Opening.
 * Left 70% = EarthMapWidget, Right 30% = controls + search + spawn confirm.
 */
public class RealEarthCreationScreen extends Screen {
    public static boolean allowVanilla = false;
    private final Screen parent;
    private EarthMapWidget mapWidget;
    private EditBox searchBox;
    private Button searchBtn;
    private Button createBtn;
    private Button cancelBtn;
    private String status = "";

    // Selected spawn
    private double selLat = 41.0082; // Istanbul default
    private double selLon = 28.9784;
    private String selLabel = "Istanbul, Turkey";
    private boolean hasSelection = true;

    public RealEarthCreationScreen(Screen parent) {
        super(Component.translatable("gui.reallifeearth.create_world"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int mapW = (int)(width * 0.68);
        int mapH = height - 24;
        int panelX = mapW + 6;
        int panelW = width - panelX - 6;

        mapWidget = new EarthMapWidget(4, 18, mapW, mapH, selLat, selLon, 6);
        mapWidget.setOnSelect((lat, lon, label) -> {
            selLat = lat; selLon = lon; selLabel = label; hasSelection = true;
            status = String.format("%.4f, %.4f - %s", lat, lon, label);
            updateCreateBtn();
        });

        searchBox = new EditBox(font, panelX, 22, panelW - 52, 18, Component.literal("Search"));
        searchBox.setHint(Component.literal("Sehir/Ulke ara..."));
        searchBox.setMaxLength(80);

        searchBtn = Button.builder(Component.literal("Ara"), b -> doSearch())
                .bounds(panelX + panelW - 48, 22, 48, 18).build();

        createBtn = Button.builder(Component.literal("Devam -> Dunya Ayarlari"), b -> doCreate())
                .bounds(panelX, height - 68, panelW, 20).build();
        Button fastBtn = Button.builder(Component.literal("§aHIZLI OLUSTUR"), b -> doFastCreate())
                .bounds(panelX, height - 44, panelW, 20).build();
        cancelBtn = Button.builder(Component.literal("Iptal"), b -> onClose())
                .bounds(panelX, height - 20, panelW, 16).build();
        addRenderableWidget(fastBtn);

        addRenderableWidget(mapWidget);
        addRenderableWidget(searchBox);
        addRenderableWidget(searchBtn);
        addRenderableWidget(createBtn);
        addRenderableWidget(cancelBtn);

        // Quick presets
        int y = 48;
        String[][] presets = {
                {"Istanbul", "41.0082,28.9784"},
                {"Ankara", "39.9334,32.8597"},
                {"Tokyo", "35.6812,139.7671"},
                {"New York", "40.7128,-74.0060"},
                {"Berlin", "52.5200,13.4050"},
                {"Paris", "48.8566,2.3522"},
        };
        for (String[] p : presets) {
            String label = p[0];
            String[] ll = p[1].split(",");
            double lat = Double.parseDouble(ll[0]), lon = Double.parseDouble(ll[1]);
            Button btn = Button.builder(Component.literal(label), b -> {
                selLat = lat; selLon = lon; selLabel = label; hasSelection = true;
                mapWidget.flyTo(lat, lon, 10);
                status = label + String.format(" (%.4f, %.4f)", lat, lon);
                updateCreateBtn();
            }).bounds(panelX, y, panelW, 16).build();
            addRenderableWidget(btn);
            y += 18;
        }

        updateCreateBtn();
        status = selLabel + String.format(" (%.4f, %.4f)", selLat, selLon);
    }

    private void updateCreateBtn() {
        if (createBtn != null) createBtn.active = hasSelection;
    }

    private void doSearch() {
        String q = searchBox.getValue().trim();
        if (q.isEmpty()) return;
        status = "Araniyor: " + q + "...";
        searchBtn.active = false;
        mapWidget.search(q, result -> {
            searchBtn.active = true;
            if (result == null) {
                status = "Sonuc bulunamadi: " + q;
            } else {
                selLat = result.lat(); selLon = result.lon(); selLabel = result.label();
                hasSelection = true;
                mapWidget.flyTo(result.lat(), result.lon(), 12);
                status = result.label() + String.format(" (%.4f, %.4f)", result.lat(), result.lon());
                updateCreateBtn();
            }
        });
    }

    private void doCreate() {
        RealEarthData.pendingLat = selLat;
        RealEarthData.pendingLon = selLon;
        RealEarthData.pendingLabel = selLabel;
        if (minecraft != null) {
            try {
                allowVanilla = true;
                net.minecraft.client.gui.screens.worldselection.CreateWorldScreen.openFresh(minecraft, parent);
            } catch (Exception e) {
                status = "Hata: vanilla ekran acilamadi (" + e.getMessage() + "). Spawn yine de kaydedildi.";
            }
        }
    }

    private void doFastCreate() {
        RealEarthData.pendingLat = selLat;
        RealEarthData.pendingLon = selLon;
        RealEarthData.pendingLabel = selLabel;
        if (minecraft != null) {
            status = "Dunya hizla olusturuluyor...";
            allowVanilla = true;
            // create vanilla world immediately with default settings, skip name/gamemode config
            // openFresh will create a world with default name; user can rename later
            net.minecraft.client.gui.screens.worldselection.CreateWorldScreen.openFresh(minecraft, parent);
            // After openFresh the new CreateWorldScreen is on screen - auto-click its Create button on next tick
            // Instead: schedule via minecraft.execute to click create
            minecraft.tell(() -> {
                var screen = minecraft.screen;
                if (screen instanceof net.minecraft.client.gui.screens.worldselection.CreateWorldScreen cws) {
                    // Find and click the "Create" button
                    for (var w : cws.children()) {
                        if (w instanceof Button btn) {
                            String txt = btn.getMessage().getString().toLowerCase();
                            if (txt.contains("create") || txt.contains("oluştur") || txt.contains("olustur")) {
                                btn.onPress();
                                break;
                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        super.render(g, mx, my, pt);
        g.drawCenteredString(font, title, width / 2, 6, 0xFFFFFF);
        int mapW = (int)(width * 0.68);
        int panelX = mapW + 6;
        g.fill(panelX - 2, 18, width - 4, height - 4, 0x88000000);
        g.drawString(font, Component.literal("Konum Sec"), panelX + 4, 10, 0xFFFFFF);
        g.drawString(font, status, panelX + 4, height - 84, 0xAAAAAA, false);
        g.drawString(font, Component.literal("Haritada tikla / surukle / zoom (tekerlek)"), 8, height - 12, 0x888888, false);
        g.drawString(font, Component.literal("Sec -> 'Devam' isim/oyun modu ayarla").withColor(0xFFCCFF88), panelX + 4, height - 96, 0xCCFF88, false);
        g.drawString(font, Component.literal("veya 'HIZLI OLUSTUR' ile direk gir!").withColor(0xFF88FF88), panelX + 4, height - 108, 0x88FF88, false);
    }

    @Override
    public boolean keyPressed(int key, int scancode, int mods) {
        if (searchBox != null && searchBox.isFocused() && key == 257) { // Enter
            doSearch(); return true;
        }
        return super.keyPressed(key, scancode, mods);
    }

    @Override
    public void onClose() {
        if (minecraft != null) minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
