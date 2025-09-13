package com.zurrtum.create.impl.effect;

import com.zurrtum.create.api.effect.OpenPipeEffectHandler;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.block.AbstractCandleBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.Entity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

import java.util.List;

public class WaterEffectHandler implements OpenPipeEffectHandler {
    @Override
    public void apply(World level, Box area, FluidStack fluid) {
        if (level.getTime() % 5 != 0)
            return;

        List<Entity> entities = level.getOtherEntities(null, area, Entity::isOnFire);
        for (Entity entity : entities)
            entity.extinguish();

        BlockPos.stream(area).forEach(pos -> dowseFire(level, pos));
    }

    // Adapted from ThrownPotion
    private static void dowseFire(World level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isIn(BlockTags.FIRE)) {
            level.removeBlock(pos, false);
        } else if (AbstractCandleBlock.isLitCandle(state)) {
            AbstractCandleBlock.extinguish(null, state, level, pos);
        } else if (CampfireBlock.isLitCampfire(state)) {
            level.syncWorldEvent(null, WorldEvents.FIRE_EXTINGUISHED, pos, 0);
            CampfireBlock.extinguish(null, level, pos, state);
            level.setBlockState(pos, state.with(CampfireBlock.LIT, false));
        }
    }
}
