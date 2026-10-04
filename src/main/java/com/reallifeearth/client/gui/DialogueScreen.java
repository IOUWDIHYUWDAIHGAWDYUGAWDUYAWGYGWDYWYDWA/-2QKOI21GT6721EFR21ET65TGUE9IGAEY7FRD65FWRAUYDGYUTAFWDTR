package com.reallifeearth.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Simple branching dialogue - data driven in FAZ3.
 * For now minimal: shows NPC name + 3 options.
 */
public class DialogueScreen extends Screen {
    private final String npcName;
    private final String profession;
    private final List<String> lines;
    private final Runnable onCloseCb;

    public DialogueScreen(String npcName, String profession, List<String> lines, Runnable onCloseCb) {
        super(Component.literal(npcName));
        this.npcName = npcName;
        this.profession = profession;
        this.lines = lines;
        this.onCloseCb = onCloseCb;
    }

    @Override
    protected void init() {
        int w = 260, h = 22;
        int x = width / 2 - w / 2;
        int y = height - 90;
        addRenderableWidget(Button.builder(Component.literal("Merhaba!"), b -> closeWith("Merhaba!"))
                .bounds(x, y, w, h).build());
        addRenderableWidget(Button.builder(Component.literal("Meslegin nedir?"), b -> closeWith("Meslegin: " + profession))
                .bounds(x, y + 26, w, h).build());
        addRenderableWidget(Button.builder(Component.literal("Gorusuruz"), b -> onClose())
                .bounds(x, y + 52, w, h).build());
    }

    private void closeWith(String msg) {
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.displayClientMessage(Component.literal(npcName + ": " + msg), false);
        }
        onClose();
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        super.render(g, mx, my, pt);
        g.drawCenteredString(font, npcName + " (" + profession + ")", width / 2, height - 112, 0xFFFFFF);
        if (!lines.isEmpty()) {
            g.drawCenteredString(font, lines.get(0), width / 2, height - 124, 0xAAAAAA);
        }
        // semi panel
        g.fill(width / 2 - 160, height - 130, width / 2 + 160, height - 98, 0x88000000);
    }

    @Override
    public void onClose() {
        if (minecraft != null) minecraft.setScreen(null);
        if (onCloseCb != null) onCloseCb.run();
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
