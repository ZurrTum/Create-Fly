package com.zurrtum.create.client.model;

import com.google.common.base.Supplier;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

public interface LayerBakedModel {
    static ChunkSectionLayer getBlockRenderLayer(SimpleModelWrapper model, Supplier<ChunkSectionLayer> defaultLayer) {
        ChunkSectionLayer layer = ((LayerBakedModel) (Object) model).create$getBlockRenderLayer();
        if (layer != null) {
            return layer;
        }
        return defaultLayer.get();
    }

    static ChunkSectionLayer getBlockRenderLayer(BlockModelPart part, Supplier<ChunkSectionLayer> defaultLayer) {
        if (part instanceof LayerBakedModel model) {
            ChunkSectionLayer layer = model.create$getBlockRenderLayer();
            if (layer != null) {
                return layer;
            }
        }
        return defaultLayer.get();
    }

    default ChunkSectionLayer create$getBlockRenderLayer() {
        return null;
    }

    default void create$setBlockRenderLayer(ChunkSectionLayer layer) {
    }
}
