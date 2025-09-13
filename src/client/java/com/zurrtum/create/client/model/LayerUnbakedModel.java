package com.zurrtum.create.client.model;

import com.google.gson.JsonObject;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.model.BakedSimpleModel;
import net.minecraft.client.render.model.GeometryBakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

public interface LayerUnbakedModel {
    static GeometryBakedModel setBlockRenderLayer(GeometryBakedModel geometry, BakedSimpleModel model) {
        if (model.getModel() instanceof LayerUnbakedModel unbakedModel) {
            BlockRenderLayer layer = unbakedModel.create$getBlockRenderLayer();
            if (layer != null) {
                ((LayerBakedModel) (Object) geometry).create$setBlockRenderLayer(layer);
            }
        }
        return geometry;
    }

    static JsonUnbakedModel setBlockRenderLayer(JsonUnbakedModel model, JsonObject jsonObject) {
        if (jsonObject.has("render_type")) {
            BlockRenderLayer layer = NamedBlockRenderLayer.get(JsonHelper.getString(jsonObject, "render_type"));
            if (layer != null) {
                ((LayerUnbakedModel) (Object) model).create$setBlockRenderLayer(layer);
            }
        }
        return model;
    }

    @Nullable
    default BlockRenderLayer create$getBlockRenderLayer() {
        return null;
    }

    default void create$setBlockRenderLayer(BlockRenderLayer layer) {
    }
}
