package com.reallifeearth.entity.npc;

import net.minecraft.util.RandomSource;

public enum NpcPersonality {
    FRIENDLY("friendly"),
    SHY("shy"),
    GRUMPY("grumpy"),
    CHEERFUL("cheerful"),
    SERIOUS("serious"),
    KIND("kind"),
    ARROGANT("arrogant"),
    FUNNY("funny");

    public final String id;
    NpcPersonality(String id) { this.id = id; }

    public static NpcPersonality random(RandomSource r) {
        return values()[r.nextInt(values().length)];
    }
}
