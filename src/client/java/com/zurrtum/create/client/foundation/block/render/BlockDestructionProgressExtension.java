package com.zurrtum.create.client.foundation.block.render;

import org.jetbrains.annotations.Nullable;

import java.util.Set;

import net.minecraft.core.BlockPos;

public interface BlockDestructionProgressExtension {
    @Nullable Set<BlockPos> create$getExtraPositions();

    void create$setExtraPositions(@Nullable Set<BlockPos> positions);
}
