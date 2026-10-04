package com.reallifeearth.client;

import com.reallifeearth.RealLifeEarthMod;
import com.reallifeearth.client.gui.RealEarthCreationScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.ArrayList;

@EventBusSubscriber(modid = RealLifeEarthMod.MODID, value = Dist.CLIENT)
public class SelectWorldScreenPatch {
    private static final Logger LOG = LogUtils.getLogger();

    @SubscribeEvent
    public static void onScreenInitPost(ScreenEvent.Init.Post e) {
        if (!(e.getScreen() instanceof SelectWorldScreen sws)) return;
        try {
            LOG.info("[RealLifeEarth] SelectWorldScreen Init.Post fired - patching Create button (screen={}x{})", sws.width, sws.height);
            // Fallback always: add big extra button at top so user has guarantee
            Button extraBtn = Button.builder(Component.literal("§bREAL EARTH §f- Harita ile Olustur"), b -> {
                LOG.info("[RealLifeEarth] EXTRA Real Earth button clicked -> RealEarthCreationScreen");
                Minecraft.getInstance().setScreen(new RealEarthCreationScreen(sws));
            }).bounds(sws.width / 2 - 100, 32, 200, 20).build();
            // We will add this after trying to replace, so it is always visible unless replaced
            boolean patched = false;
            int targetX = sws.width / 2 + 4;
            int targetY = sws.height - 52;
            for (GuiEventListener l : new ArrayList<>(e.getListenersList())) {
                if (l instanceof Button btn) {
                    String msg = "";
                    try { msg = btn.getMessage().getString(); } catch (Exception ignored) {}
                    LOG.info("[RealLifeEarth]   button '{}' at {},{} {}x{}", msg, btn.getX(), btn.getY(), btn.getWidth(), btn.getHeight());
                    boolean isCreateBtn = false;
                    // position match (most reliable, language independent)
                    if (btn.getX() == targetX && btn.getY() == targetY && btn.getWidth() == 150) isCreateBtn = true;
                    // also text match fallback
                    if (!isCreateBtn) {
                        String raw = btn.getMessage().getString();
                        if (raw.contains("Create") || raw.contains("Olu") || raw.contains("Yeni D") || raw.toLowerCase().contains("create")) isCreateBtn = true;
                    }
                    if (isCreateBtn) {
                        int x = btn.getX(), y = btn.getY(), w = btn.getWidth(), h = btn.getHeight();
                        LOG.info("[RealLifeEarth]   -> MATCH create button '{}' - replacing onClick to open RealEarthCreationScreen", msg);
                        e.removeListener(btn);
                        Button replacement = Button.builder(Component.translatable("selectWorld.create"), b2 -> {
                            LOG.info("[RealLifeEarth] Patched Create button clicked -> RealEarthCreationScreen");
                            try {
                                Minecraft.getInstance().setScreen(new RealEarthCreationScreen(sws));
                            } catch (Throwable t) {
                                LOG.error("[RealLifeEarth] Failed to open RealEarthCreationScreen", t);
                            }
                        }).bounds(x, y, w, h).build();
                        e.addListener(replacement);
                        patched = true;
                        break;
                    }
                }
            }
            if (!patched) {
                LOG.warn("[RealLifeEarth] Create button NOT found by position/text - adding extra button as only entry");
            } else {
                LOG.info("[RealLifeEarth] Create button patched successfully");
            }
            // Always add the extra top button as guaranteed entry (won't hurt)
            e.addListener(extraBtn);
            LOG.info("[RealLifeEarth] Added guaranteed top button 'REAL EARTH - HARITA ILE OLUSTUR'");
        } catch (Throwable t) {
            LOG.error("[RealLifeEarth] SelectWorldScreenPatch FAILED", t);
        }
    }
}
