package com.zurrtum.create.client.flywheel.backend;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public interface FlwBackendXplat {
    FlwBackendXplat INSTANCE = new FlwBackendXplatImpl();

    int getLightEmission(BlockState state, BlockView level, BlockPos pos);
}
