package com.zurrtum.create.client.catnip.render;

import com.zurrtum.create.client.foundation.model.BakedModelHelper;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.BakedQuad.MaterialInfo;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

public class ShiftStitchedSprite extends StitchedSprite {
    protected final StitchedSprite original;
    private float u, v, diffU, diffV, width, height;
    private @Nullable ChunkSectionLayer layer;
    private @UnknownNullability SpriteContents contents;

    public ShiftStitchedSprite(Identifier atlas, StitchedSprite original, Identifier location) {
        super(atlas, location);
        this.original = original;
    }

    @SuppressWarnings("deprecation")
    public ShiftStitchedSprite(StitchedSprite original, Identifier location) {
        this(TextureAtlas.LOCATION_BLOCKS, original, location);
    }

    @Override
    protected void loadSprite(TextureAtlas atlas) {
        sprite = atlas.getSprite(location);
        if (sprite.transparency().isOpaque()) {
            layer = ChunkSectionLayer.SOLID;
            contents = null;
        } else {
            layer = null;
            contents = sprite.contents();
        }
        layer = sprite.transparency().isOpaque() ? ChunkSectionLayer.SOLID : null;
        TextureAtlasSprite originalSprite = original.get();
        u = originalSprite.getU0();
        v = originalSprite.getV0();
        diffU = sprite.getU0() - u;
        diffV = sprite.getV0() - v;
        width = originalSprite.getU1() - u;
        height = originalSprite.getV1() - v;
    }

    public Identifier getOriginalLocation() {
        return original.location;
    }

    public TextureAtlasSprite getOriginal() {
        return original.sprite;
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
        ChunkSectionLayer layer = this.layer;
        if (layer == null) {
            layer = ChunkSectionLayer.byTransparency(contents.computeTransparency(
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
