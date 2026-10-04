package com.reallifeearth.entity.npc;

import com.reallifeearth.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;

public class NpcSpawner {
    public static int spawnNear(ServerLevel level, BlockPos center, int count) {
        int spawned = 0;
        var rng = level.getRandom();
        for (int i = 0; i < count; i++) {
            int dx = rng.nextInt(40) - 20;
            int dz = rng.nextInt(40) - 20;
            BlockPos p = center.offset(dx, 0, dz);
            int y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, p.getX(), p.getZ());
            BlockPos spawn = new BlockPos(p.getX(), y, p.getZ());
            var e = ModEntities.REAL_NPC.get().create(level);
            if (e == null) continue;
            e.moveTo(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, rng.nextFloat()*360, 0);
            e.finalizeSpawn(level, level.getCurrentDifficultyAt(spawn), MobSpawnType.COMMAND, null);
            e.setHomePos(spawn);
            level.addFreshEntity(e);
            spawned++;
        }
        return spawned;
    }
}
