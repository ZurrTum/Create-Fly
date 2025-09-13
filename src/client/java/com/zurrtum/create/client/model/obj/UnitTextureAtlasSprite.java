/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package com.zurrtum.create.client.model.obj;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.texture.SpriteDimensions;
import net.minecraft.resource.metadata.ResourceMetadata;
import net.minecraft.util.Identifier;

/**
 * A helper sprite with UVs spanning the entire texture.
 * <p>
 * Useful for baking quads that won't be used with an atlas.
 */
public class UnitTextureAtlasSprite extends Sprite {
    public static final Identifier LOCATION = Identifier.of("neoforge", "unit");
    public static final UnitTextureAtlasSprite INSTANCE = new UnitTextureAtlasSprite();

    private UnitTextureAtlasSprite() {
        super(LOCATION, new SpriteContents(LOCATION, new SpriteDimensions(1, 1), new NativeImage(1, 1, false), ResourceMetadata.NONE), 1, 1, 0, 0);
    }

    @Override
    public float getFrameU(float u) {
        return u;
    }

    @Override
    public float getFrameV(float v) {
        return v;
    }
}
