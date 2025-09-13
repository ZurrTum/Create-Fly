package com.zurrtum.create.client.model;

import com.google.common.base.Supplier;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.GeometryBakedModel;

public interface LayerBakedModel {
    static BlockRenderLayer getBlockRenderLayer(GeometryBakedModel model, Supplier<BlockRenderLayer> defaultLayer) {
        BlockRenderLayer layer = ((LayerBakedModel) (Object) model).create$getBlockRenderLayer();
        if (layer != null) {
            return layer;
        }
        return defaultLayer.get();
    }

    static BlockRenderLayer getBlockRenderLayer(BlockModelPart part, Supplier<BlockRenderLayer> defaultLayer) {
        if (part instanceof LayerBakedModel model) {
            BlockRenderLayer layer = model.create$getBlockRenderLayer();
            if (layer != null) {
                return layer;
            }
        }
        return defaultLayer.get();
    }

    default BlockRenderLayer create$getBlockRenderLayer() {
        return null;
    }

    default void create$setBlockRenderLayer(BlockRenderLayer layer) {
    }
}
