package com.reallifeearth.worldgen;

import com.reallifeearth.osm.BuildingPalette;
import com.reallifeearth.osm.OsmToMcConverter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Places OSM buildings and roads into the world.
 * Call from command or chunk handler. Very simple extruded polygon.
 */
public class BuildingPlacer {

    public static int placeBuildings(ServerLevel level, List<OsmToMcConverter.Building> buildings, BlockPos origin) {
        int placed = 0;
        for (var b : buildings) {
            try {
                placeSingle(level, b, origin);
                placed++;
                if (placed > 800) break; // safety
            } catch (Exception ignored) {}
        }
        return placed;
    }

    private static void placeSingle(ServerLevel level, OsmToMcConverter.Building b, BlockPos origin) {
        var palette = BuildingPalette.forType(b.type());
        BlockState wall = palette.wall().defaultBlockState();
        BlockState floor = palette.floor().defaultBlockState();
        BlockState roof = palette.roof().defaultBlockState();
        BlockState window = palette.window().defaultBlockState();

        // compute polygon bounds
        var poly = b.polygon();
        int minX = poly.stream().mapToInt(BlockPos::getX).min().orElse(0);
        int maxX = poly.stream().mapToInt(BlockPos::getX).max().orElse(0);
        int minZ = poly.stream().mapToInt(BlockPos::getZ).min().orElse(0);
        int maxZ = poly.stream().mapToInt(BlockPos::getZ).max().orElse(0);
        if (maxX - minX < 3 || maxZ - minZ < 3) return;
        if (maxX - minX > 60 || maxZ - minZ > 60) return; // skip huge

        int groundY = findGround(level, origin.offset(minX, 0, minZ));
        if (groundY < level.getMinBuildHeight()) groundY = level.getSeaLevel();
        int height = Math.max(4, b.levels() * 4); // 1 floor = 4 blocks

        // Fill floor
        for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++) {
            if (!pointInPolygon(x, z, poly)) continue;
            BlockPos p = origin.offset(x, groundY, z);
            level.setBlock(p, floor, 3);
            // foundation below
            for (int y = 1; y < 4; y++) {
                BlockPos below = p.below(y);
                if (level.getBlockState(below).isAir() || level.getBlockState(below).canBeReplaced())
                    level.setBlock(below, Blocks.STONE.defaultBlockState(), 3);
            }
        }

        // Walls
        for (int y = 1; y < height; y++) {
            for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++) {
                if (!pointInPolygon(x, z, poly)) continue;
                boolean isEdge = !pointInPolygon(x+1,z,poly) || !pointInPolygon(x-1,z,poly) || !pointInPolygon(x,z+1,poly) || !pointInPolygon(x,z-1,poly);
                if (!isEdge) continue;
                BlockPos p = origin.offset(x, groundY + y, z);
                // windows on floor 2
                boolean isWindow = y >= 2 && y <= 3 && (x + z) % 3 == 0;
                level.setBlock(p, isWindow ? window : wall, 3);
            }
        }

        // Roof - flat
        for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++) {
            if (!pointInPolygon(x, z, poly)) continue;
            BlockPos p = origin.offset(x, groundY + height, z);
            level.setBlock(p, roof, 3);
        }

        // Door on south edge
        for (int x = minX; x <= maxX; x++) {
            int z = maxZ;
            BlockPos edge = origin.offset(x, groundY + 1, z);
            if (level.getBlockState(edge).is(wall.getBlock())) {
                level.setBlock(edge, Blocks.AIR.defaultBlockState(), 3);
                level.setBlock(edge.above(), Blocks.AIR.defaultBlockState(), 3);
                // door block
                level.setBlock(edge, Blocks.OAK_DOOR.defaultBlockState(), 3);
                break;
            }
        }
    }

    public static int placeRoads(ServerLevel level, List<OsmToMcConverter.Road> roads, BlockPos origin) {
        int placed = 0;
        for (var r : roads) {
            var pts = r.points();
            for (int i = 0; i < pts.size() - 1; i++) {
                var a = pts.get(i); var b = pts.get(i+1);
                drawRoadSegment(level, origin, a, b, r.width());
            }
            placed++;
            if (placed > 600) break;
        }
        return placed;
    }

    private static void drawRoadSegment(ServerLevel level, BlockPos origin, BlockPos a, BlockPos b, int width) {
        int dx = b.getX() - a.getX(), dz = b.getZ() - a.getZ();
        int steps = Math.max(Math.abs(dx), Math.abs(dz));
        if (steps == 0) return;
        for (int s = 0; s <= steps; s++) {
            float t = (float)s / steps;
            int x = Math.round(a.getX() + dx * t);
            int z = Math.round(a.getZ() + dz * t);
            for (int wx = -width/2; wx <= width/2; wx++) for (int wz = -width/2; wz <= width/2; wz++) {
                if (width > 3 && Math.abs(wx) == width/2 && Math.abs(wz) == width/2) continue;
                BlockPos p = origin.offset(x + wx, 0, z + wz);
                int y = findGround(level, p);
                BlockPos roadPos = new BlockPos(p.getX(), y, p.getZ());
                BlockState road = (wx == 0 && width >= 5) ? Blocks.WHITE_CONCRETE.defaultBlockState() : Blocks.GRAY_CONCRETE.defaultBlockState();
                // try custom asphalt
                try {
                    var id = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("reallifeearth","asphalt");
                    var holder = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getHolder(id);
                    if (holder.isPresent()) {
                        var asphaltBlock = holder.get().value();
                        if (asphaltBlock != Blocks.AIR) road = asphaltBlock.defaultBlockState();
                    }
                } catch (Exception ignored) {}
                level.setBlock(roadPos, road, 3);
                // sidewalk on edges
                if (Math.abs(wx) == width/2 || Math.abs(wz) == width/2) {
                    // keep road, add curb below? skip
                }
                // clear above
                level.setBlock(roadPos.above(), Blocks.AIR.defaultBlockState(), 3);
                level.setBlock(roadPos.above(2), Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    private static int findGround(ServerLevel level, BlockPos x_z) {
        int y = level.getSeaLevel() + 20;
        // scan down to find solid
        for (int scan = y; scan > level.getMinBuildHeight() + 5; scan--) {
            BlockPos p = new BlockPos(x_z.getX(), scan, x_z.getZ());
            if (!level.getBlockState(p).isAir() && level.getBlockState(p).isSolidRender(level, p)) {
                return scan + 1;
            }
        }
        return level.getSeaLevel();
    }

    private static boolean pointInPolygon(int x, int z, List<BlockPos> poly) {
        boolean inside = false;
        for (int i = 0, j = poly.size() - 1; i < poly.size(); j = i++) {
            int xi = poly.get(i).getX(), zi = poly.get(i).getZ();
            int xj = poly.get(j).getX(), zj = poly.get(j).getZ();
            if (((zi > z) != (zj > z)) && (x < (xj - xi) * (z - zi) / (float)(zj - zi) + xi)) inside = !inside;
        }
        return inside;
    }
}
