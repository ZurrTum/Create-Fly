package com.zurrtum.create.api.schematic.state;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public interface SchematicStateFilter {
    /**
     * This will always be called from the logical server
     */
    BlockState filterStates(@Nullable BlockEntity be, BlockState state);
}
