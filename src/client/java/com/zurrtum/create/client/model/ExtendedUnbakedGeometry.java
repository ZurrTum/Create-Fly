/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package com.zurrtum.create.client.model;

import net.minecraft.client.render.model.*;
import net.minecraft.util.context.ContextParameterMap;

/**
 * Base interface for unbaked models that wish to support the NeoForge-added {@code bake} method
 */
@FunctionalInterface
public interface ExtendedUnbakedGeometry extends Geometry {
    ContextParameterMap EMPTY = new ContextParameterMap.Builder().build(NeoForgeModelProperties.EMPTY_TYPE);

    @Override
    default BakedGeometry bake(ModelTextures p_405831_, Baker p_405026_, ModelBakeSettings p_405122_, SimpleModel p_405635_) {
        return bake(p_405831_, p_405026_, p_405122_, p_405635_, EMPTY);
    }

    // Re-abstract the extended version
    BakedGeometry bake(
        ModelTextures textureSlots,
        Baker baker,
        ModelBakeSettings state,
        SimpleModel debugName,
        ContextParameterMap additionalProperties
    );
}
