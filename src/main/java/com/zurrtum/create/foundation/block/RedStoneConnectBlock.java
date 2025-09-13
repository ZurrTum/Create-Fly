package com.zurrtum.create.foundation.block;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public interface RedStoneConnectBlock {
    boolean canConnectRedstone(BlockState state, @Nullable Direction side);
}
