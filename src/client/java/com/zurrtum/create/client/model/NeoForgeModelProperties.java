/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package com.zurrtum.create.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.context.ContextParameter;
import net.minecraft.util.context.ContextParameterMap;
import net.minecraft.util.context.ContextType;
import net.minecraft.util.math.AffineTransformation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Properties that NeoForge adds for {@link JsonUnbakedModel}s and {@link UnbakedModel}s.
 */
public final class NeoForgeModelProperties {
    private NeoForgeModelProperties() {
    }

    /**
     * Root transform. For block models, this can be specified under the {@code transform} JSON key.
     */
    public static final ContextParameter<AffineTransformation> TRANSFORM = ContextParameter.of("transform");

    /**
     * Render type to use. For block models, this can be specified under the {@code render_type} JSON key.
     */
    public static final ContextParameter<RenderLayer> RENDER_TYPE = ContextParameter.of("render_type");

    /**
     * Part visibilities. For models with named parts (i.e. OBJ and composite), this can be specified under the {@code visibility} JSON key
     */
    public static final ContextParameter<Map<String, Boolean>> PART_VISIBILITY = ContextParameter.of("part_visibility");

    public static final ContextType EMPTY_TYPE = new ContextType.Builder().build();
    public static final ContextType TYPE = new ContextType.Builder().allow(TRANSFORM).allow(RENDER_TYPE).allow(PART_VISIBILITY).build();

    /**
     * {@return a {@link AffineTransformation} if the {@code transform} key is present, otherwise {@code null}}
     */
    @Nullable
    public static AffineTransformation deserializeRootTransform(JsonObject jsonObject, JsonDeserializationContext context) {
        if (jsonObject.has("transform")) {
            JsonElement transform = jsonObject.get("transform");
            return context.deserialize(transform, AffineTransformation.class);
        }
        return null;
    }

    @Nullable
    public static BlockRenderLayer deserializeRenderType(JsonObject jsonObject) {
        if (jsonObject.has("render_type")) {
            return NamedBlockRenderLayer.get(JsonHelper.getString(jsonObject, "render_type"));
        }
        return null;
    }

    /**
     * {@return a map of part visibilities if the {@code visibility} key is present, otherwise an empty map}
     */
    public static Map<String, Boolean> deserializePartVisibility(JsonObject jsonObject) {
        Map<String, Boolean> partVisibility = new HashMap<>();
        if (jsonObject.has("visibility")) {
            JsonObject visibility = JsonHelper.getObject(jsonObject, "visibility");
            for (Map.Entry<String, JsonElement> part : visibility.entrySet()) {
                partVisibility.put(part.getKey(), part.getValue().getAsBoolean());
            }
        }
        return Map.copyOf(partVisibility);
    }

    /**
     * Puts the given {@linkplain AffineTransformation root transform} into the given builder if present, overwriting any value specified in a parent model
     */
    public static void fillRootTransformProperty(ContextParameterMap.Builder propertiesBuilder, @Nullable AffineTransformation rootTransform) {
        if (rootTransform != null) {
            propertiesBuilder.add(NeoForgeModelProperties.TRANSFORM, rootTransform);
        }
    }

    /**
     * Puts the given part visibility into the given builder if present, merging the with values from parent models
     * on a per-key basis and overwriting existing keys
     */
    public static void fillPartVisibilityProperty(ContextParameterMap.Builder propertiesBuilder, Map<String, Boolean> partVisibility) {
        if (!partVisibility.isEmpty()) {
            Map<String, Boolean> visibility = propertiesBuilder.getNullable(NeoForgeModelProperties.PART_VISIBILITY);
            if (visibility != null) {
                visibility = new HashMap<>(visibility);
                visibility.putAll(partVisibility);
            } else {
                visibility = partVisibility;
            }
            visibility = Map.copyOf(visibility);
            propertiesBuilder.add(NeoForgeModelProperties.PART_VISIBILITY, visibility);
        }
    }
}
