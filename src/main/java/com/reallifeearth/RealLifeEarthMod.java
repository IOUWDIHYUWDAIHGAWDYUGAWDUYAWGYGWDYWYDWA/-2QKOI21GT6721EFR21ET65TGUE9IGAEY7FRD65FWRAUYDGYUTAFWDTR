package com.reallifeearth;

import com.reallifeearth.config.RealLifeConfig;
import com.reallifeearth.registry.ModAttachments;
import com.reallifeearth.registry.ModBlocks;
import com.reallifeearth.registry.ModCreativeTabs;
import com.reallifeearth.registry.ModEntities;
import com.reallifeearth.registry.ModItems;
import com.reallifeearth.worldgen.RealEarthData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

@Mod(RealLifeEarthMod.MODID)
public class RealLifeEarthMod {
    public static final String MODID = "reallifeearth";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RealLifeEarthMod(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);
        ModAttachments.ATTACHMENTS.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, RealLifeConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, RealLifeConfig.CLIENT_SPEC);

        NeoForge.EVENT_BUS.register(RealEarthData.class);
        NeoForge.EVENT_BUS.register(com.reallifeearth.command.RealLifeCommands.class);
        NeoForge.EVENT_BUS.register(com.reallifeearth.needs.NeedsEvents.class);

        LOGGER.info("[RealLifeEarth] Mod initialized - Gercek Hayat Basliyor!");
    }
}
