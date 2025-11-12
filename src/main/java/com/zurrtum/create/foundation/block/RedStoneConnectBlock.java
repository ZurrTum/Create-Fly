package com.zurrtum.create.foundation.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface RedStoneConnectBlock {
    boolean canConnectRedstone(BlockState state, @Nullable Direction side);
}
