package com.reallifeearth.osm;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class BuildingPalette {
    public record Palette(Block wall, Block floor, Block roof, Block window) {}

    public static Palette forType(String buildingType) {
        return switch (buildingType) {
            case "apartments","residential","house","detached","semidetached_house" ->
                    new Palette(Blocks.BRICKS, Blocks.OAK_PLANKS, Blocks.BRICK_STAIRS, Blocks.GLASS_PANE);
            case "retail","supermarket","shop","commercial" ->
                    new Palette(Blocks.QUARTZ_BLOCK, Blocks.POLISHED_ANDESITE, Blocks.SMOOTH_QUARTZ, Blocks.GLASS);
            case "office" -> new Palette(Blocks.GRAY_CONCRETE, Blocks.POLISHED_DIORITE, Blocks.STONE_BRICKS, Blocks.GLASS_PANE);
            case "industrial","warehouse" -> new Palette(Blocks.GRAY_CONCRETE, Blocks.STONE, Blocks.IRON_BLOCK, Blocks.GLASS_PANE);
            case "hospital","clinic" -> new Palette(Blocks.WHITE_CONCRETE, Blocks.WHITE_TERRACOTTA, Blocks.SMOOTH_QUARTZ, Blocks.GLASS);
            case "school","university","college" -> new Palette(Blocks.BRICKS, Blocks.OAK_PLANKS, Blocks.BRICK_STAIRS, Blocks.GLASS_PANE);
            case "church","cathedral","mosque","temple" -> new Palette(Blocks.SANDSTONE, Blocks.SMOOTH_SANDSTONE, Blocks.SANDSTONE_STAIRS, Blocks.GLASS_PANE);
            default -> new Palette(Blocks.STONE_BRICKS, Blocks.OAK_PLANKS, Blocks.COBBLESTONE, Blocks.GLASS_PANE);
        };
    }
}
