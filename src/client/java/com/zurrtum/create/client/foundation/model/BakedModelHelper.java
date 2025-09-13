package com.zurrtum.create.client.foundation.model;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.VecHelper;
import net.minecraft.client.render.model.BakedGeometry;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.GeometryBakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;

import static com.zurrtum.create.client.catnip.render.SpriteShiftEntry.getUnInterpolatedU;
import static com.zurrtum.create.client.catnip.render.SpriteShiftEntry.getUnInterpolatedV;

public class BakedModelHelper {

    public static int[] cropAndMove(int[] vertexData, Sprite sprite, Box crop, Vec3d move) {
        vertexData = Arrays.copyOf(vertexData, vertexData.length);

        Vec3d xyz0 = BakedQuadHelper.getXYZ(vertexData, 0);
        Vec3d xyz1 = BakedQuadHelper.getXYZ(vertexData, 1);
        Vec3d xyz2 = BakedQuadHelper.getXYZ(vertexData, 2);
        Vec3d xyz3 = BakedQuadHelper.getXYZ(vertexData, 3);

        Vec3d uAxis = xyz3.add(xyz2).multiply(.5);
        Vec3d vAxis = xyz1.add(xyz2).multiply(.5);
        Vec3d center = xyz3.add(xyz2).add(xyz0).add(xyz1).multiply(.25);

        float u0 = BakedQuadHelper.getU(vertexData, 0);
        float u3 = BakedQuadHelper.getU(vertexData, 3);
        float v0 = BakedQuadHelper.getV(vertexData, 0);
        float v1 = BakedQuadHelper.getV(vertexData, 1);

        float uScale = (float) Math.round((getUnInterpolatedU(sprite, u3) - getUnInterpolatedU(sprite, u0)) / xyz3.distanceTo(xyz0));
        float vScale = (float) Math.round((getUnInterpolatedV(sprite, v1) - getUnInterpolatedV(sprite, v0)) / xyz1.distanceTo(xyz0));

        if (uScale == 0) {
            float v3 = BakedQuadHelper.getV(vertexData, 3);
            float u1 = BakedQuadHelper.getU(vertexData, 1);
            uAxis = xyz1.add(xyz2).multiply(.5);
            vAxis = xyz3.add(xyz2).multiply(.5);
            uScale = (float) Math.round((getUnInterpolatedU(sprite, u1) - getUnInterpolatedU(sprite, u0)) / xyz1.distanceTo(xyz0));
            vScale = (float) Math.round((getUnInterpolatedV(sprite, v3) - getUnInterpolatedV(sprite, v0)) / xyz3.distanceTo(xyz0));

        }

        uAxis = uAxis.subtract(center).normalize();
        vAxis = vAxis.subtract(center).normalize();

        Vec3d min = new Vec3d(crop.minX, crop.minY, crop.minZ);
        Vec3d max = new Vec3d(crop.maxX, crop.maxY, crop.maxZ);

        for (int vertex = 0; vertex < 4; vertex++) {
            Vec3d xyz = BakedQuadHelper.getXYZ(vertexData, vertex);
            Vec3d newXyz = VecHelper.componentMin(max, VecHelper.componentMax(xyz, min));
            Vec3d diff = newXyz.subtract(xyz);

            if (diff.lengthSquared() > 0) {
                float u = BakedQuadHelper.getU(vertexData, vertex);
                float v = BakedQuadHelper.getV(vertexData, vertex);
                float uDiff = (float) uAxis.dotProduct(diff) * uScale;
                float vDiff = (float) vAxis.dotProduct(diff) * vScale;
                BakedQuadHelper.setU(vertexData, vertex, sprite.getFrameU(getUnInterpolatedU(sprite, u) + uDiff));
                BakedQuadHelper.setV(vertexData, vertex, sprite.getFrameV(getUnInterpolatedV(sprite, v) + vDiff));
            }

            BakedQuadHelper.setXYZ(vertexData, vertex, newXyz.add(move));
        }

        return vertexData;
    }

    public static GeometryBakedModel generateModel(GeometryBakedModel template, UnaryOperator<Sprite> spriteSwapper) {
        BakedGeometry.Builder builder = new BakedGeometry.Builder();
        for (Direction cullFace : Iterate.directions) {
            List<BakedQuad> quads = template.getQuads(cullFace);
            swapSprites(quads, spriteSwapper).forEach(quad -> builder.add(cullFace, quad));
        }

        List<BakedQuad> quads = template.getQuads(null);
        swapSprites(quads, spriteSwapper).forEach(builder::add);

        Sprite particleSprite = template.particleSprite();
        Sprite swappedParticleSprite = spriteSwapper.apply(particleSprite);
        if (swappedParticleSprite != null) {
            particleSprite = swappedParticleSprite;
        }
        return new GeometryBakedModel(builder.build(), template.useAmbientOcclusion(), particleSprite);
    }

    public static List<BakedQuad> swapSprites(List<BakedQuad> quads, UnaryOperator<Sprite> spriteSwapper) {
        List<BakedQuad> newQuads = new ArrayList<>(quads);
        int size = quads.size();
        for (int i = 0; i < size; i++) {
            BakedQuad quad = quads.get(i);
            Sprite sprite = quad.sprite();
            Sprite newSprite = spriteSwapper.apply(sprite);
            if (newSprite == null || sprite == newSprite)
                continue;

            BakedQuad newQuad = BakedQuadHelper.clone(quad);
            int[] vertexData = newQuad.vertexData();

            for (int vertex = 0; vertex < 4; vertex++) {
                float u = BakedQuadHelper.getU(vertexData, vertex);
                float v = BakedQuadHelper.getV(vertexData, vertex);
                BakedQuadHelper.setU(vertexData, vertex, newSprite.getFrameU(getUnInterpolatedU(sprite, u)));
                BakedQuadHelper.setV(vertexData, vertex, newSprite.getFrameV(getUnInterpolatedV(sprite, v)));
            }

            newQuads.set(i, newQuad);
        }
        return newQuads;
    }
}