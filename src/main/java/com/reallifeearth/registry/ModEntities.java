package com.reallifeearth.registry;

import com.reallifeearth.RealLifeEarthMod;
import com.reallifeearth.entity.npc.RealNpcEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, RealLifeEarthMod.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<RealNpcEntity>> REAL_NPC = ENTITIES.register("real_npc",
            () -> EntityType.Builder.<RealNpcEntity>of(RealNpcEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(64)
                    .build("real_npc"));

    // Spawn egg deferred - created after entity registration
    public static DeferredHolder<net.minecraft.world.item.Item, net.minecraft.world.item.SpawnEggItem> NPC_SPAWN_EGG;

    static {
        // Will be registered via DeferredRegister.Items lazily - but we create holder here for tab
        // Actual registration done in ModItems after this class loads
    }
}
