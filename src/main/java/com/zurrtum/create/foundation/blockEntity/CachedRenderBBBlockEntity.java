package com.zurrtum.create.foundation.blockEntity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public abstract class CachedRenderBBBlockEntity extends SyncedBlockEntity {

    private Box renderBoundingBox;

    public CachedRenderBBBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Environment(EnvType.CLIENT)
    public Box getRenderBoundingBox() {
        if (renderBoundingBox == null) {
            renderBoundingBox = createRenderBoundingBox();
        }
        return renderBoundingBox;
    }

    protected void invalidateRenderBoundingBox() {
        renderBoundingBox = null;
    }

    protected Box createRenderBoundingBox() {
        return new Box(getPos());
    }

}
