package com.reallifeearth.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class PhoneScreen extends Screen {
    public PhoneScreen() { super(Component.literal("Telefon")); }

    @Override
    protected void init() {
        int cx = width / 2, cy = height / 2;
        addRenderableWidget(Button.builder(Component.literal("Harita"), b -> {})
                .bounds(cx - 60, cy - 40, 120, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Banka"), b -> {})
                .bounds(cx - 60, cy - 16, 120, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Kisiler"), b -> {})
                .bounds(cx - 60, cy + 8, 120, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Kapat"), b -> onClose())
                .bounds(cx - 60, cy + 32, 120, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        super.render(g, mx, my, pt);
        g.drawCenteredString(font, "Telefon - v1.0", width / 2, height / 2 - 60, 0xFFFFFF);
        // phone frame
        g.fill(width / 2 - 70, height / 2 - 70, width / 2 + 70, height / 2 + 60, 0xCC111111);
        g.renderOutline(width / 2 - 70, height / 2 - 70, 140, 130, 0xFF444444);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
