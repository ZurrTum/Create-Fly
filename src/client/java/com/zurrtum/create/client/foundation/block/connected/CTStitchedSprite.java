package com.zurrtum.create.client.foundation.block.connected;

import com.mojang.blaze3d.platform.Transparency;
import com.zurrtum.create.client.catnip.render.StitchedSprite;
import com.zurrtum.create.client.foundation.model.BakedModelHelper;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.BakedQuad.MaterialInfo;
import net.minecraft.resources.Identifier;

public class CTStitchedSprite extends StitchedSprite {
    private final Identifier originalLocation;
    private float u, v, diffU, diffV, width, height;

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
            u = sprite.getU0();
            v = sprite.getV0();
            width = sprite.getU1() - u;
            height = sprite.getV1() - v;
            return;
        }
        TextureAtlasSprite original = atlas.getSprite(originalLocation);
        u = original.getU0();
        v = original.getV0();
        diffU = sprite.getU0() - u;
        diffV = sprite.getV0() - v;
        width = original.getU1() - u;
        height = original.getV1() - v;
    }

    public float getTargetU(float localU) {
        return localU + diffU;
    }

    public float getTargetV(float localV) {
        return localV + diffV;
    }

    public BakedQuad replaceQuad(BakedQuad quad, MaterialInfo info) {
        long uv = quad.packedUV0();
        float u0 = UVPair.unpackU(uv);
        float v0 = UVPair.unpackV(uv);
        uv = quad.packedUV1();
        float u1 = UVPair.unpackU(uv);
        float v1 = UVPair.unpackV(uv);
        uv = quad.packedUV2();
        float u2 = UVPair.unpackU(uv);
        float v2 = UVPair.unpackV(uv);
        uv = quad.packedUV3();
        float u3 = UVPair.unpackU(uv);
        float v3 = UVPair.unpackV(uv);
        Transparency transparency = sprite.transparency();
        ChunkSectionLayer layer;
        if (transparency.isOpaque()) {
            layer = ChunkSectionLayer.SOLID;
        } else {
            layer = ChunkSectionLayer.byTransparency(sprite.contents().computeTransparency(
                (Math.min(Math.min(u0, u1), Math.min(u2, u3)) - u) / width,
                (Math.min(Math.min(v0, v1), Math.min(v2, v3)) - v) / height,
                (Math.max(Math.max(u0, u1), Math.max(u2, u3)) - u) / width,
                (Math.max(Math.max(v0, v1), Math.max(v2, v3)) - v) / height
            ));
        }
        if (info.layer() != layer) {
            info = new MaterialInfo(
                sprite,
                layer,
                info.itemRenderType(),
                info.tintIndex(),
                info.shade(),
                info.lightEmission()
            );
        }
        return BakedModelHelper.replaceBakedQuadUV(
            quad,
            UVPair.pack(u0 + diffU, v0 + diffV),
            UVPair.pack(u1 + diffU, v1 + diffV),
            UVPair.pack(u2 + diffU, v2 + diffV),
            UVPair.pack(u3 + diffU, v3 + diffV),
            info
        );
    }
}
