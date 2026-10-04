package com.reallifeearth.registry;

import com.reallifeearth.RealLifeEarthMod;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(RealLifeEarthMod.MODID);

    public static final DeferredBlock<Block> ASPHALT = BLOCKS.registerSimpleBlock("asphalt",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(1.5f, 6f).requiresCorrectToolForDrops());
    public static final DeferredBlock<Block> ASPHALT_LINE = BLOCKS.registerSimpleBlock("asphalt_line",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(1.5f, 6f));
    public static final DeferredBlock<Block> SIDEWALK = BLOCKS.registerSimpleBlock("sidewalk",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(1.2f, 6f));
    public static final DeferredBlock<Block> CONCRETE_WHITE = BLOCKS.registerSimpleBlock("concrete_white",
            BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).strength(1.2f, 6f));
    public static final DeferredBlock<Block> SHOP_SHELF = BLOCKS.registerSimpleBlock("shop_shelf",
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0f));
    public static final DeferredBlock<Block> OFFICE_DESK = BLOCKS.registerSimpleBlock("office_desk",
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0f));
}
