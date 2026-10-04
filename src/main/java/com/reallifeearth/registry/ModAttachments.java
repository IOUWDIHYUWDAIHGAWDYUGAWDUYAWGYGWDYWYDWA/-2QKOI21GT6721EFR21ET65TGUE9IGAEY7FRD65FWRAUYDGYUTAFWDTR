package com.reallifeearth.registry;

import com.mojang.serialization.Codec;
import com.reallifeearth.RealLifeEarthMod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, RealLifeEarthMod.MODID);

    // Player attachments
    public static final Supplier<AttachmentType<Double>> WALLET = ATTACHMENTS.register("wallet",
            () -> AttachmentType.builder(() -> 500.0).serialize(Codec.DOUBLE).copyOnDeath().build());

    public static final Supplier<AttachmentType<Integer>> THIRST = ATTACHMENTS.register("thirst",
            () -> AttachmentType.builder(() -> 100).serialize(Codec.INT).copyOnDeath().build());
    public static final Supplier<AttachmentType<Integer>> ENERGY = ATTACHMENTS.register("energy",
            () -> AttachmentType.builder(() -> 100).serialize(Codec.INT).copyOnDeath().build());
    public static final Supplier<AttachmentType<Integer>> HYGIENE = ATTACHMENTS.register("hygiene",
            () -> AttachmentType.builder(() -> 100).serialize(Codec.INT).copyOnDeath().build());

    // Simple string map for relationships: "uuid->score" serialized as string
    public static final Supplier<AttachmentType<String>> RELATIONSHIPS = ATTACHMENTS.register("relationships",
            () -> AttachmentType.builder(() -> "").serialize(Codec.STRING).copyOnDeath().build());
}
