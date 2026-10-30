/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package com.zurrtum.create.client.model;

import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.util.context.ContextMap;

/**
 * Base interface for unbaked models that wish to support the NeoForge-added {@code bake} method
 */
@FunctionalInterface
public interface ExtendedUnbakedGeometry extends UnbakedGeometry {
    @Override
    default QuadCollection bake(
        TextureSlots p_405831_,
        ModelBaker p_405026_,
        ModelState p_405122_,
        ModelDebugName p_405635_
    ) {
        return bake(p_405831_, p_405026_, p_405122_, p_405635_, ContextMap.EMPTY);
    }

    // Re-abstract the extended version
    QuadCollection bake(
        TextureSlots textureSlots,
        ModelBaker baker,
        ModelState state,
        ModelDebugName debugName,
        ContextMap additionalProperties
    );
}
