package com.reallifeearth.economy;

import com.reallifeearth.registry.ModAttachments;
import net.minecraft.world.entity.player.Player;

public class WalletManager {
    public static double get(Player p) {
        return p.getData(ModAttachments.WALLET);
    }
    public static void set(Player p, double v) {
        p.setData(ModAttachments.WALLET, Math.max(0, v));
    }
    public static boolean add(Player p, double delta) {
        double cur = get(p);
        double next = cur + delta;
        if (next < 0) return false;
        set(p, next);
        return true;
    }
    public static boolean pay(Player from, Player to, double amount) {
        if (amount <= 0) return false;
        if (get(from) < amount) return false;
        add(from, -amount);
        add(to, amount);
        return true;
    }
}
