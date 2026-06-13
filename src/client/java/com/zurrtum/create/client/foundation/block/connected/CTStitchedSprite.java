package com.zurrtum.create.client.foundation.block.connected;

import com.zurrtum.create.client.catnip.render.StitchedSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;

public class CTStitchedSprite extends StitchedSprite {
    private final Identifier originalLocation;
    private float diffU;
    private float diffV;

    public CTStitchedSprite(Identifier originalLocation) {
        super(originalLocation);
        this.originalLocation = originalLocation;
    }

    public CTStitchedSprite(Identifier originalLocation, Identifier targetLocation) {
        super(targetLocation);
        this.originalLocation = originalLocation;
    }

    @Override
    protected void loadSprite(TextureAtlas atlas) {
        sprite = atlas.getSprite(location);
        if (location == originalLocation) {
            return;
        }
        TextureAtlasSprite original = atlas.getSprite(originalLocation);
        diffU = sprite.getU0() - original.getU0();
        diffV = sprite.getV0() - original.getV0();
    }

    public float getTargetU(float localU) {
        return localU + diffU;
    }

    public float getTargetV(float localV) {
        return localV + diffV;
    }
}
