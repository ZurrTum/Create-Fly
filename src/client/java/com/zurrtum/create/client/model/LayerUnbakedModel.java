package com.zurrtum.create.client.model;

import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

public interface LayerUnbakedModel {
    static BlockModelPart setBlockRenderLayer(BlockModelPart geometry, ResolvedModel model) {
        if (model.wrapped() instanceof LayerUnbakedModel unbakedModel) {
            ChunkSectionLayer layer = unbakedModel.create$getBlockRenderLayer();
            if (layer != null) {
                ((LayerBakedModel) geometry).create$setBlockRenderLayer(layer);
            }
        }
        return geometry;
    }

    static BlockModel setBlockRenderLayer(BlockModel model, JsonObject jsonObject) {
        if (jsonObject.has("render_type")) {
            ChunkSectionLayer layer = NamedBlockRenderLayer.get(GsonHelper.getAsString(jsonObject, "render_type"));
            if (layer != null) {
                ((LayerUnbakedModel) (Object) model).create$setBlockRenderLayer(layer);
            }
        }
        return model;
    }

    @Nullable
    default ChunkSectionLayer create$getBlockRenderLayer() {
        return null;
    }

    default void create$setBlockRenderLayer(ChunkSectionLayer layer) {
    }
}
