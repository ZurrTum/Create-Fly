/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package com.zurrtum.create.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.model.ModelTextures;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.AffineTransformation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Wrapper around all standard top-level model parameters added by vanilla and NeoForge except elements.
 * <p>
 * For use in custom model loaders which want to respect these properties but create the quads from
 * something other than the vanilla elements spec.
 */
@SuppressWarnings("deprecation")
public record StandardModelParameters(
    @Nullable Identifier parent, ModelTextures.Textures textures, @Nullable ModelTransformation itemTransforms, @Nullable Boolean ambientOcclusion,
    @Nullable UnbakedModel.GuiLight guiLight, @Nullable AffineTransformation rootTransform, @Nullable BlockRenderLayer layer,
    Map<String, Boolean> partVisibility
) {
    public static StandardModelParameters parse(JsonObject jsonObject, JsonDeserializationContext context) {
        String parentName = JsonHelper.getString(jsonObject, "parent", "");
        Identifier parent = parentName.isEmpty() ? null : Identifier.of(parentName);

        ModelTextures.Textures textures = ModelTextures.Textures.EMPTY;
        if (jsonObject.has("textures")) {
            JsonObject jsonobject = JsonHelper.getObject(jsonObject, "textures");
            textures = ModelTextures.fromJson(jsonobject, SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
        }

        ModelTransformation itemTransforms = null;
        if (jsonObject.has("display")) {
            JsonObject jsonobject1 = JsonHelper.getObject(jsonObject, "display");
            itemTransforms = context.deserialize(jsonobject1, ModelTransformation.class);
        }

        Boolean ambientOcclusion = null;
        if (jsonObject.has("ambientocclusion")) {
            ambientOcclusion = JsonHelper.getBoolean(jsonObject, "ambientocclusion");
        }

        UnbakedModel.GuiLight guiLight = null;
        if (jsonObject.has("gui_light")) {
            guiLight = UnbakedModel.GuiLight.byName(JsonHelper.getString(jsonObject, "gui_light"));
        }

        AffineTransformation rootTransform = NeoForgeModelProperties.deserializeRootTransform(jsonObject, context);
        BlockRenderLayer layer = NeoForgeModelProperties.deserializeRenderType(jsonObject);
        Map<String, Boolean> partVisibility = NeoForgeModelProperties.deserializePartVisibility(jsonObject);

        return new StandardModelParameters(parent, textures, itemTransforms, ambientOcclusion, guiLight, rootTransform, layer, partVisibility);
    }
}
