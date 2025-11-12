/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package com.zurrtum.create.client.model;

import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.resources.model.*;
import net.minecraft.util.context.ContextMap;

/**
 * Base interface for unbaked models that wish to support the NeoForge-added {@code bake} method
 */
@FunctionalInterface
public interface ExtendedUnbakedGeometry extends UnbakedGeometry {
    ContextMap EMPTY = new ContextMap.Builder().create(NeoForgeModelProperties.EMPTY_TYPE);

    @Override
    default QuadCollection bake(TextureSlots p_405831_, ModelBaker p_405026_, ModelState p_405122_, ModelDebugName p_405635_) {
        return bake(p_405831_, p_405026_, p_405122_, p_405635_, EMPTY);
    }

    // Re-abstract the extended version
    QuadCollection bake(TextureSlots textureSlots, ModelBaker baker, ModelState state, ModelDebugName debugName, ContextMap additionalProperties);
}
