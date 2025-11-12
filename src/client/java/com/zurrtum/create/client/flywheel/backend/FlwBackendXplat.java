package com.zurrtum.create.client.flywheel.backend;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface FlwBackendXplat {
    FlwBackendXplat INSTANCE = new FlwBackendXplatImpl();

    int getLightEmission(BlockState state, BlockGetter level, BlockPos pos);
}
