package com.zurrtum.create.client.foundation.model;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.model.NormalsBakedQuad;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import static com.zurrtum.create.client.catnip.render.SpriteShiftEntry.getUnInterpolatedU;
import static com.zurrtum.create.client.catnip.render.SpriteShiftEntry.getUnInterpolatedV;

public class BakedModelHelper {
    private static long calcSpriteUv(Vec3 diff, long packedUV, Vec3 uAxis, Vec3 vAxis, float uScale, float vScale, TextureAtlasSprite sprite) {
        if (diff.lengthSqr() == 0) {
            return packedUV;
        }
        float u = UVPair.unpackU(packedUV);
        float v = UVPair.unpackV(packedUV);
        u = sprite.getU(getUnInterpolatedU(sprite, u) + ((float) uAxis.dot(diff) * uScale));
        v = sprite.getV(getUnInterpolatedV(sprite, v) + ((float) vAxis.dot(diff) * vScale));
        return UVPair.pack(u, v);
    }

    public static BakedQuad cropAndMove(BakedQuad quad, AABB crop, Vec3 move) {
        TextureAtlasSprite sprite = quad.sprite();

        Vec3 xyz0 = new Vec3(quad.position0());
        Vec3 xyz1 = new Vec3(quad.position1());
        Vec3 xyz2 = new Vec3(quad.position2());
        Vec3 xyz3 = new Vec3(quad.position3());
        long packedUV0 = quad.packedUV0();
        long packedUV1 = quad.packedUV1();
        long packedUV2 = quad.packedUV2();
        long packedUV3 = quad.packedUV3();

        Vec3 uAxis = xyz3.add(xyz2).scale(.5);
        Vec3 vAxis = xyz1.add(xyz2).scale(.5);
        Vec3 center = xyz3.add(xyz2).add(xyz0).add(xyz1).scale(.25);

        float u0 = UVPair.unpackU(packedUV0);
        float u3 = UVPair.unpackU(packedUV3);
        float v0 = UVPair.unpackV(packedUV0);
        float v1 = UVPair.unpackV(packedUV1);

        float uScale = (float) Math.round((getUnInterpolatedU(sprite, u3) - getUnInterpolatedU(sprite, u0)) / xyz3.distanceTo(xyz0));
        float vScale = (float) Math.round((getUnInterpolatedV(sprite, v1) - getUnInterpolatedV(sprite, v0)) / xyz1.distanceTo(xyz0));

        if (uScale == 0) {
            float v3 = UVPair.unpackV(packedUV3);
            float u1 = UVPair.unpackU(packedUV1);
            uAxis = xyz1.add(xyz2).scale(.5);
            vAxis = xyz3.add(xyz2).scale(.5);
            uScale = (float) Math.round((getUnInterpolatedU(sprite, u1) - getUnInterpolatedU(sprite, u0)) / xyz1.distanceTo(xyz0));
            vScale = (float) Math.round((getUnInterpolatedV(sprite, v3) - getUnInterpolatedV(sprite, v0)) / xyz3.distanceTo(xyz0));
        }

        uAxis = uAxis.subtract(center).normalize();
        vAxis = vAxis.subtract(center).normalize();

        Vec3 min = new Vec3(crop.minX, crop.minY, crop.minZ);
        Vec3 max = new Vec3(crop.maxX, crop.maxY, crop.maxZ);
        Vec3 newXyz0 = VecHelper.componentMin(max, VecHelper.componentMax(xyz0, min));
        Vec3 newXyz1 = VecHelper.componentMin(max, VecHelper.componentMax(xyz1, min));
        Vec3 newXyz2 = VecHelper.componentMin(max, VecHelper.componentMax(xyz2, min));
        Vec3 newXyz3 = VecHelper.componentMin(max, VecHelper.componentMax(xyz3, min));
        BakedQuad newQuad = new BakedQuad(
            newXyz0.add(move).toVector3f(),
            newXyz1.add(move).toVector3f(),
            newXyz2.add(move).toVector3f(),
            newXyz3.add(move).toVector3f(),
            calcSpriteUv(newXyz0.subtract(xyz0), packedUV0, uAxis, vAxis, uScale, vScale, sprite),
            calcSpriteUv(newXyz1.subtract(xyz1), packedUV1, uAxis, vAxis, uScale, vScale, sprite),
            calcSpriteUv(newXyz2.subtract(xyz2), packedUV2, uAxis, vAxis, uScale, vScale, sprite),
            calcSpriteUv(newXyz3.subtract(xyz3), packedUV3, uAxis, vAxis, uScale, vScale, sprite),
            quad.tintIndex(),
            quad.direction(),
            quad.sprite(),
            quad.shade(),
            quad.lightEmission()
        );
        NormalsBakedQuad.setNormals(newQuad, NormalsBakedQuad.getNormals(quad));
        return newQuad;
    }

    public static SimpleModelWrapper generateModel(SimpleModelWrapper template, UnaryOperator<TextureAtlasSprite> spriteSwapper) {
        QuadCollection.Builder builder = new QuadCollection.Builder();
        for (Direction cullFace : Iterate.directions) {
            List<BakedQuad> quads = template.getQuads(cullFace);
            swapSprites(quads, spriteSwapper).forEach(quad -> builder.addCulledFace(cullFace, quad));
        }

        List<BakedQuad> quads = template.getQuads(null);
        swapSprites(quads, spriteSwapper).forEach(builder::addUnculledFace);

        TextureAtlasSprite particleSprite = template.particleIcon();
        TextureAtlasSprite swappedParticleSprite = spriteSwapper.apply(particleSprite);
        if (swappedParticleSprite != null) {
            particleSprite = swappedParticleSprite;
        }
        return new SimpleModelWrapper(builder.build(), template.useAmbientOcclusion(), particleSprite);
    }

    public static long calcSpriteUv(long packedUv, TextureAtlasSprite sprite, TextureAtlasSprite newSprite) {
        float u = newSprite.getU(getUnInterpolatedU(sprite, UVPair.unpackU(packedUv)));
        float v = newSprite.getV(getUnInterpolatedV(sprite, UVPair.unpackV(packedUv)));
        return UVPair.pack(u, v);
    }

    public static List<BakedQuad> swapSprites(List<BakedQuad> quads, UnaryOperator<TextureAtlasSprite> spriteSwapper) {
        List<BakedQuad> newQuads = new ArrayList<>(quads);
        int size = quads.size();
        for (int i = 0; i < size; i++) {
            BakedQuad quad = quads.get(i);
            TextureAtlasSprite sprite = quad.sprite();
            TextureAtlasSprite newSprite = spriteSwapper.apply(sprite);
            if (newSprite == null || sprite == newSprite)
                continue;

            BakedQuad newQuad = new BakedQuad(
                quad.position0(),
                quad.position1(),
                quad.position2(),
                quad.position3(),
                calcSpriteUv(quad.packedUV0(), sprite, newSprite),
                calcSpriteUv(quad.packedUV1(), sprite, newSprite),
                calcSpriteUv(quad.packedUV2(), sprite, newSprite),
                calcSpriteUv(quad.packedUV3(), sprite, newSprite),
                quad.tintIndex(),
                quad.direction(),
                quad.sprite(),
                quad.shade(),
                quad.lightEmission()
            );
            NormalsBakedQuad.setNormals(newQuad, NormalsBakedQuad.getNormals(quad));
            newQuads.set(i, newQuad);
        }
        return newQuads;
    }
}