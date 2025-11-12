/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package com.zurrtum.create.client.model.obj;

import com.zurrtum.create.client.model.AbstractUnbakedModel;
import com.zurrtum.create.client.model.LayerUnbakedModel;
import com.zurrtum.create.client.model.StandardModelParameters;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.resources.model.UnbakedGeometry;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * A model loaded from an OBJ file.
 * <p>
 * Supports positions, texture coordinates, normals and colors. The {@link ObjMaterialLibrary material library}
 * has support for numerous features, including support for {@link ResourceLocation} textures (non-standard).
 */
public class ObjModel extends AbstractUnbakedModel implements LayerUnbakedModel {
    private final ObjGeometry geometry;

    public ObjModel(StandardModelParameters parameters, ObjGeometry geometry) {
        super(parameters);
        this.geometry = geometry;
    }

    @Override
    public UnbakedGeometry geometry() {
        return geometry;
    }

    @Override
    @Nullable
    public ChunkSectionLayer create$getBlockRenderLayer() {
        return parameters.layer();
    }
}
