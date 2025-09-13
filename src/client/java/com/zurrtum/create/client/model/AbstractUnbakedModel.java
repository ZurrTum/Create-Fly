/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package com.zurrtum.create.client.model;

import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * Base unbaked model for custom models which support the standard top-level model parameters
 * added by vanilla and NeoForge except elements but create the quads from something other
 * than the vanilla elements spec.
 */
public abstract class AbstractUnbakedModel implements UnbakedModel {
    /**
     * Holds the standard top-level model parameters except elements.
     * {@link UnbakedGeometry#bake(ModelTextures, Baker, ModelBakeSettings, SimpleModel)}
     * must always use the values given as parameters instead of accessing this parameter directly in order to
     * take values collected along the model's parent chain into account.
     */
    protected final StandardModelParameters parameters;

    protected AbstractUnbakedModel(StandardModelParameters parameters) {
        this.parameters = parameters;
    }

    @Nullable
    @Override
    public Boolean ambientOcclusion() {
        return this.parameters.ambientOcclusion();
    }

    @Nullable
    @Override
    public GuiLight guiLight() {
        return this.parameters.guiLight();
    }

    @Nullable
    @Override
    public ModelTransformation transformations() {
        return this.parameters.itemTransforms();
    }

    @Override
    public ModelTextures.Textures textures() {
        return this.parameters.textures();
    }

    @Nullable
    @Override
    public Identifier parent() {
        return this.parameters.parent();
    }
}
