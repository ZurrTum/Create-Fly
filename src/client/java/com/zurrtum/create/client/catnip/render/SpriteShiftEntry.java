package com.zurrtum.create.client.catnip.render;

import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class SpriteShiftEntry {
    @Nullable
    protected StitchedSprite original;
    @Nullable
    protected StitchedSprite target;

    public void set(Identifier originalLocation, Identifier targetLocation) {
        original = new StitchedSprite(originalLocation);
        target = new StitchedSprite(targetLocation);
    }

    public Identifier getOriginalResourceLocation() {
        Objects.requireNonNull(original);
        return original.getLocation();
    }

    public Identifier getTargetResourceLocation() {
        Objects.requireNonNull(target);
        return target.getLocation();
    }

    public Sprite getOriginal() {
        Objects.requireNonNull(original);
        return original.get();
    }

    public Sprite getTarget() {
        Objects.requireNonNull(target);
        return target.get();
    }

    public float getTargetU(float localU) {
        return getTarget().getFrameU(getUnInterpolatedU(getOriginal(), localU));
    }

    public float getTargetV(float localV) {
        return getTarget().getFrameV(getUnInterpolatedV(getOriginal(), localV));
    }

    public static float getUnInterpolatedU(Sprite sprite, float u) {
        float f = sprite.getMaxU() - sprite.getMinU();
        return (u - sprite.getMinU()) / f;
    }

    public static float getUnInterpolatedV(Sprite sprite, float v) {
        float f = sprite.getMaxV() - sprite.getMinV();
        return (v - sprite.getMinV()) / f;
    }
}
