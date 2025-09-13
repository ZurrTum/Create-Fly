/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package com.zurrtum.create.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;

/**
 * A loader for custom {@linkplain UnbakedModel unbaked models}.
 * <p>
 */
public interface UnbakedModelLoader<T extends UnbakedModel> {
    /**
     * Reads an unbaked model from the passed JSON object.
     *
     * <p>The {@link JsonDeserializationContext} argument can be used to deserialize types that the system already understands.
     * For example, {@code deserializationContext.deserialize(<sub object>, Transformation.class)} to parse a transformation,
     * or {@code deserializationContext.deserialize(<sub object>, UnbakedModel.class)} to parse a nested model.
     * The set of supported types can be found in the declaration of {@link JsonUnbakedModel#GSON}.
     */
    T read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException;
}
