package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.model.LayerUnbakedModel;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockModel.class)
public class JsonUnbakedModelMixin implements LayerUnbakedModel {
    @Unique
    private ChunkSectionLayer blockRenderLayer;

    @Override
    @Nullable
    public ChunkSectionLayer create$getBlockRenderLayer() {
        return blockRenderLayer;
    }

    @Override
    public void create$setBlockRenderLayer(ChunkSectionLayer blockRenderLayer) {
        this.blockRenderLayer = blockRenderLayer;
    }
}
