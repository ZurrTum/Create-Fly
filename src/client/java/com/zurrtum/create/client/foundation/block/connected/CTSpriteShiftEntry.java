package com.zurrtum.create.client.foundation.block.connected;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;

public class CTSpriteShiftEntry {
    protected final CTType type;
    protected final CTStitchedSprite original;
    protected final CTStitchedSprite[] targets;

    public CTSpriteShiftEntry(CTType type, Identifier originalLocation, Identifier targetLocation) {
        this.type = type;
        int size = type.getSpriteSize();
        targets = new CTStitchedSprite[size];
        targets[0] = original = new CTStitchedSprite(originalLocation);
        for (int i = type.replaceOriginal() ? 0 : 1; i < size; i++) {
            targets[i] = new CTStitchedSprite(originalLocation, targetLocation.withSuffix("/" + i));
        }
    }

    public TextureAtlasSprite getOriginal() {
        return original.get();
    }

    public TextureAtlasSprite getTarget(int index) {
        return targets[index].get();
    }

    public CTType getType() {
        return type;
    }

    public float getTargetU(float localU, int index) {
        return targets[index].getTargetU(localU);
    }

    public float getTargetV(float localV, int index) {
        return targets[index].getTargetV(localV);
    }
}
