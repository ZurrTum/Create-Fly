package com.zurrtum.create.api.contraption.transformable;

import com.zurrtum.create.content.contraptions.StructureTransform;
import net.minecraft.block.BlockState;

public interface TransformableBlock {
    BlockState transform(BlockState state, StructureTransform transform);
}
