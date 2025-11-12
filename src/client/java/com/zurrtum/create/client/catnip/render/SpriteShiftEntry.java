package com.zurrtum.create.client.catnip.render;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;

public class SpriteShiftEntry {
    @Nullable
    protected StitchedSprite original;
    @Nullable
    protected StitchedSprite target;

    public void set(Identifier originalLocation, Identifier targetLocation) {
        original = new StitchedSprite(originalLocation);
        target = new StitchedSprite(targetLocation);
    }

    public Identifier getOriginalIdentifier() {
        Objects.requireNonNull(original);
        return original.getLocation();
    }

    public Identifier getTargetIdentifier() {
        Objects.requireNonNull(target);
        return target.getLocation();
    }

    public TextureAtlasSprite getOriginal() {
        Objects.requireNonNull(original);
        return original.get();
    }

    public TextureAtlasSprite getTarget() {
        Objects.requireNonNull(target);
        return target.get();
    }

    public float getTargetU(float localU) {
        return getTarget().getU(getUnInterpolatedU(getOriginal(), localU));
    }

    public float getTargetV(float localV) {
        return getTarget().getV(getUnInterpolatedV(getOriginal(), localV));
    }

    public static float getUnInterpolatedU(TextureAtlasSprite sprite, float u) {
        float f = sprite.getU1() - sprite.getU0();
        return (u - sprite.getU0()) / f;
    }

    public static float getUnInterpolatedV(TextureAtlasSprite sprite, float v) {
        float f = sprite.getV1() - sprite.getV0();
        return (v - sprite.getV0()) / f;
    }
}
