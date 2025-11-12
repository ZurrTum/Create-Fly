package com.zurrtum.create.api.contraption.transformable;

import com.zurrtum.create.content.contraptions.StructureTransform;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface TransformableBlockEntity {
    void transform(BlockEntity blockEntity, StructureTransform transform);
}
