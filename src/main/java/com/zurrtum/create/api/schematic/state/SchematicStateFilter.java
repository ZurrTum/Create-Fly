package com.zurrtum.create.api.schematic.state;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface SchematicStateFilter {
    /**
     * This will always be called from the logical server
     */
    BlockState filterStates(@Nullable BlockEntity be, BlockState state);
}
