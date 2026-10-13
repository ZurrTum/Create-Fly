package com.zurrtum.create.client.foundation.block.connected;

import com.zurrtum.create.client.catnip.render.ShiftStitchedSprite;
import com.zurrtum.create.client.catnip.render.StitchedSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.BakedQuad.MaterialInfo;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public class CTStitchedSprite extends ShiftStitchedSprite {
    @SuppressWarnings("DataFlowIssue")
    public CTStitchedSprite(Identifier originalLocation) {
        super(null, originalLocation);
    }

    @SuppressWarnings("DataFlowIssue")
    public CTStitchedSprite(@Nullable StitchedSprite originalLocation, Identifier targetLocation) {
        super(originalLocation, targetLocation);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void loadSprite(TextureAtlas atlas) {
        if (original == null) {
            sprite = atlas.getSprite(location);
            return;
        }
        super.loadSprite(atlas);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public BakedQuad replaceQuad(BakedQuad quad, MaterialInfo info) {
        if (original == null) {
            return quad;
        }
        return super.replaceQuad(quad, info);
    }

    @SuppressWarnings("ConstantConditions")
    public Identifier getOriginalLocation() {
        if (original == null) {
            return location;
        }
        return super.getOriginalLocation();
    }

    @SuppressWarnings("ConstantConditions")
    public TextureAtlasSprite getOriginal() {
        if (original == null) {
            return sprite;
        }
        return super.getOriginal();
    }
}
