package com.reallifeearth.client;

import com.reallifeearth.RealLifeEarthMod;
import com.reallifeearth.client.gui.RealEarthCreationScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

@EventBusSubscriber(modid = RealLifeEarthMod.MODID, value = Dist.CLIENT)
public class ScreenInterceptor {
    private static final Logger LOG = LogUtils.getLogger();

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening e) {
        try {
            if (e.getNewScreen() instanceof CreateWorldScreen) {
                LOG.info("[RealLifeEarth] Intercept CreateWorldScreen -> RealEarthCreationScreen (allowVanilla={})", RealEarthCreationScreen.allowVanilla);
                if (RealEarthCreationScreen.allowVanilla) {
                    RealEarthCreationScreen.allowVanilla = false;
                    LOG.info("[RealLifeEarth] Allowing vanilla CreateWorldScreen this time");
                    return;
                }
                // Prevent loop if we are already on our screen
                if (Minecraft.getInstance().screen instanceof RealEarthCreationScreen) {
                    LOG.info("[RealLifeEarth] Already on RealEarthCreationScreen, skip intercept");
                    return;
                }
                RealEarthCreationScreen replacement = new RealEarthCreationScreen(e.getCurrentScreen());
                e.setNewScreen(replacement);
                LOG.info("[RealLifeEarth] Replaced with RealEarthCreationScreen");
            }
        } catch (Throwable t) {
            LOG.error("[RealLifeEarth] ScreenInterceptor FAILED", t);
        }
    }
}
