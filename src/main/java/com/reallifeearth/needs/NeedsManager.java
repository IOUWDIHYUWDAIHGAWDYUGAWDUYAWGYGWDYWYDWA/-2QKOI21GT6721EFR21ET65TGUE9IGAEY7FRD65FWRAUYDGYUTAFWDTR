package com.reallifeearth.needs;

import com.reallifeearth.config.RealLifeConfig;
import com.reallifeearth.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class NeedsManager {
    public static void tick(ServerPlayer p) {
        if (!RealLifeConfig.ENABLE_NEEDS.get()) return;
        // Very slow decay: thirst -1 per 2 minutes, energy -1 per 3 minutes, hygiene -1 per 5 minutes
        long gameTime = p.level().getGameTime();
        if (gameTime % (20 * 120) == 0) {
            int t = p.getData(ModAttachments.THIRST);
            if (t > 0) p.setData(ModAttachments.THIRST, t - 1);
            if (t <= 10) p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 0, true, false));
        }
        if (gameTime % (20 * 180) == 0) {
            int e = p.getData(ModAttachments.ENERGY);
            if (!p.isSleeping() && e > 0) p.setData(ModAttachments.ENERGY, e - 1);
        }
        if (gameTime % (20 * 300) == 0) {
            int h = p.getData(ModAttachments.HYGIENE);
            if (h > 0) p.setData(ModAttachments.HYGIENE, h - 1);
        }
        // Sleep restores energy
        if (p.isSleeping() && gameTime % 20 == 0) {
            int e = p.getData(ModAttachments.ENERGY);
            if (e < 100) p.setData(ModAttachments.ENERGY, Math.min(100, e + 2));
        }
    }

    public static void drink(ServerPlayer p, int amount) {
        int t = p.getData(ModAttachments.THIRST);
        p.setData(ModAttachments.THIRST, Math.min(100, t + amount));
    }
    public static void wash(ServerPlayer p) {
        p.setData(ModAttachments.HYGIENE, 100);
    }
}
