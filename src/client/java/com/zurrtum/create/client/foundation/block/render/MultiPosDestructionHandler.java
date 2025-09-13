package com.zurrtum.create.client.foundation.block.render;

import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface MultiPosDestructionHandler {
    /**
     * Returned set must be mutable and must not be changed after it is returned.
     */
    @Nullable Set<BlockPos> getExtraPositions(ClientWorld level, BlockPos pos, BlockState blockState, int progress);
}
