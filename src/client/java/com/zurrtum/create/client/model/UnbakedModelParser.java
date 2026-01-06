/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package com.zurrtum.create.client.model;

import com.google.gson.*;
import com.google.gson.internal.bind.JsonTreeReader;
import com.google.gson.internal.bind.TreeTypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.mojang.math.Transformation;
import com.zurrtum.create.client.model.obj.ObjLoader;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.UnbakedModel;

import java.lang.reflect.Type;

public class UnbakedModelParser {
    public static Gson wrap(Gson gson) {
        return new GsonBuilder().registerTypeAdapterFactory(new Deserializer(gson))
            .registerTypeAdapter(Transformation.class, new TransformationHelper.Deserializer()).create();
    }

    public static class Deserializer implements JsonDeserializer<UnbakedModel>, TypeAdapterFactory {
        private static final TypeToken<? extends UnbakedModel> NEXT_TYPE = FabricLoader.getInstance()
            .isModLoaded("fabric-model-loading-api-v1") ? TypeToken.get(UnbakedModel.class) : TypeToken.get(BlockModel.class);
        private final Gson gson;
        private TypeAdapter<?> cached;

        public Deserializer(Gson gson) {
            this.gson = gson;
        }

        @Override
        public UnbakedModel deserialize(
            JsonElement jsonElement,
            Type type,
            JsonDeserializationContext jsonDeserializationContext
        ) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonElement element = jsonObject.get("loader");
            if (element != null && element.isJsonPrimitive() && element.getAsString().equals("neoforge:obj")) {
                return ObjLoader.INSTANCE.read(jsonObject, jsonDeserializationContext);
            }
            return gson.fromJson(new JsonTreeReader(jsonObject), NEXT_TYPE);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> TypeAdapter<T> create(Gson proxy, TypeToken<T> type) {
            if (type.getRawType() == UnbakedModel.class) {
                if (cached != null) {
                    return (TypeAdapter<T>) cached;
                }
                TreeTypeAdapter<T> adapter = new TreeTypeAdapter<>(null, (JsonDeserializer<T>) this, proxy, type, this);
                cached = adapter;
                return adapter;
            }
            return gson.getAdapter(type);
        }
    }
}
