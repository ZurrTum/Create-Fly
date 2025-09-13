package com.zurrtum.create.client.flywheel.backend;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;


public class FlwBackendXplatImpl implements FlwBackendXplat {
    @Override
    public int getLightEmission(BlockState state, BlockView level, BlockPos pos) {
        return state.getLuminance();
    }
}
