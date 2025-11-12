/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package com.zurrtum.create.client.model;

import com.google.common.collect.Maps;
import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Transformation;
import com.zurrtum.create.Create;
import com.zurrtum.create.client.model.obj.ObjLoader;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.stream.Stream;

public class UnbakedModelParser {
    public static final Gson GSON = new GsonBuilder().registerTypeHierarchyAdapter(UnbakedModel.class, new Deserializer())
        .registerTypeAdapter(BlockModel.class, new BlockModel.Deserializer()).registerTypeAdapter(BlockElement.class, new BlockElement.Deserializer())
        .registerTypeAdapter(BlockElementFace.class, new BlockElementFace.Deserializer())
        .registerTypeAdapter(ItemTransform.class, new ItemTransform.Deserializer())
        .registerTypeAdapter(ItemTransforms.class, new ItemTransforms.Deserializer())
        .registerTypeAdapter(Transformation.class, new TransformationHelper.Deserializer()).create();
    private static final Map<Identifier, UnbakedModel> CACHE = Maps.newConcurrentMap();

    public static void cache(Identifier id, UnbakedModel model) {
        CACHE.put(id, model);
    }

    public static Stream<Pair<Identifier, UnbakedModel>> getCaches() {
        return CACHE.entrySet().stream().map(entry -> Pair.of(entry.getKey(), entry.getValue()));
    }

    public static final class Deserializer implements JsonDeserializer<UnbakedModel> {
        @Override
        public UnbakedModel deserialize(
            JsonElement jsonElement,
            Type type,
            JsonDeserializationContext jsonDeserializationContext
        ) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            if (jsonObject.has("loader")) {
                String loader = GsonHelper.getAsString(jsonObject, "loader");
                if (loader.equals("neoforge:obj")) {
                    return ObjLoader.INSTANCE.read(jsonObject, jsonDeserializationContext);
                }
                Create.LOGGER.warn("Unsupported loader: " + loader);
            }

            return jsonDeserializationContext.deserialize(jsonObject, BlockModel.class);
        }
    }
}
