package com.zurrtum.create.content.trains.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/**
 * Removes all Carriage entities in chunks that aren't ticking
 */
public class CarriageEntityHandler {

    public static void onEntityEnterSection(Entity entity, long oldPos, long newPos) {
        if (SectionPos.x(oldPos) == SectionPos.x(newPos) && SectionPos.z(oldPos) == SectionPos.z(newPos))
            return;
        if (!(entity instanceof CarriageContraptionEntity cce))
            return;
        if (!((ServerLevel) entity.level()).isPositionEntityTicking(SectionPos.of(newPos).center()))
            cce.leftTickingChunks = true;
    }

    public static void validateCarriageEntity(CarriageContraptionEntity entity) {
        if (!entity.isAlive())
            return;
        Level level = entity.level();
        if (level.isClientSide())
            return;
        if (!isActiveChunk(level, entity.blockPosition()))
            entity.leftTickingChunks = true;
    }

    public static boolean isActiveChunk(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel)
            return serverLevel.isPositionEntityTicking(pos);
        return false;
    }

}
