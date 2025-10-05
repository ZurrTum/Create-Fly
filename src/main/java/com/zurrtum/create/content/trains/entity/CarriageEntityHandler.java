package com.zurrtum.create.content.trains.entity;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;

/**
 * Removes all Carriage entities in chunks that aren't ticking
 */
public class CarriageEntityHandler {

    public static void onEntityEnterSection(Entity entity, long oldPos, long newPos) {
        if (ChunkSectionPos.unpackX(oldPos) == ChunkSectionPos.unpackX(newPos) && ChunkSectionPos.unpackZ(oldPos) == ChunkSectionPos.unpackZ(newPos))
            return;
        if (!(entity instanceof CarriageContraptionEntity cce))
            return;
        if (!((ServerWorld) entity.getEntityWorld()).shouldTickEntityAt(ChunkSectionPos.from(newPos).getCenterPos()))
            cce.leftTickingChunks = true;
    }

    public static void validateCarriageEntity(CarriageContraptionEntity entity) {
        if (!entity.isAlive())
            return;
        World level = entity.getEntityWorld();
        if (level.isClient())
            return;
        if (!isActiveChunk(level, entity.getBlockPos()))
            entity.leftTickingChunks = true;
    }

    public static boolean isActiveChunk(World level, BlockPos pos) {
        if (level instanceof ServerWorld serverLevel)
            return serverLevel.shouldTickEntityAt(pos);
        return false;
    }

}
