package com.zurrtum.create.client.foundation.model;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.model.NormalsBakedQuad;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.BakedQuad.MaterialInfo;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import static com.zurrtum.create.client.catnip.render.SpriteShiftEntry.getUnInterpolatedU;
import static com.zurrtum.create.client.catnip.render.SpriteShiftEntry.getUnInterpolatedV;

public class BakedModelHelper {
    private static long calcSpriteUv(
        Vec3 diff,
        long packedUV,
        Vec3 uAxis,
        Vec3 vAxis,
        float uScale,
        float vScale,
        TextureAtlasSprite sprite
    ) {
        if (diff.lengthSqr() == 0) {
            return packedUV;
        }
        float u = UVPair.unpackU(packedUV);
        float v = UVPair.unpackV(packedUV);
        u = sprite.getU(getUnInterpolatedU(sprite, u) + (float) uAxis.dot(diff) * uScale);
        v = sprite.getV(getUnInterpolatedV(sprite, v) + (float) vAxis.dot(diff) * vScale);
        return UVPair.pack(u, v);
    }

    public static BakedQuad cropAndMove(BakedQuad quad, AABB crop, Vec3 move) {
        MaterialInfo info = quad.materialInfo();
        TextureAtlasSprite sprite = info.sprite();

        Vec3 xyz0 = new Vec3(quad.position0());
        Vec3 xyz1 = new Vec3(quad.position1());
        Vec3 xyz2 = new Vec3(quad.position2());
        Vec3 xyz3 = new Vec3(quad.position3());
        long packedUV0 = quad.packedUV0();
        long packedUV1 = quad.packedUV1();
        long packedUV2 = quad.packedUV2();
        long packedUV3 = quad.packedUV3();

        Vec3 uAxis = xyz3.add(xyz2).scale(0.5);
        Vec3 vAxis = xyz1.add(xyz2).scale(0.5);
        Vec3 center = xyz3.add(xyz2).add(xyz0).add(xyz1).scale(0.25);

        float u0 = UVPair.unpackU(packedUV0);
        float u3 = UVPair.unpackU(packedUV3);
        float v0 = UVPair.unpackV(packedUV0);
        float v1 = UVPair.unpackV(packedUV1);

        float uScale = (float) ((getUnInterpolatedU(sprite, u3) - getUnInterpolatedU(
            sprite,
            u0
        )) / xyz3.distanceTo(xyz0));
        float vScale = (float) ((getUnInterpolatedV(sprite, v1) - getUnInterpolatedV(
            sprite,
            v0
        )) / xyz1.distanceTo(xyz0));

        if (uScale == 0) {
            float v3 = UVPair.unpackV(packedUV3);
            float u1 = UVPair.unpackU(packedUV1);
            uAxis = xyz1.add(xyz2).scale(0.5);
            vAxis = xyz3.add(xyz2).scale(0.5);
            uScale = (float) ((getUnInterpolatedU(sprite, u1) - getUnInterpolatedU(
                sprite,
                u0
            )) / xyz1.distanceTo(xyz0));
            vScale = (float) ((getUnInterpolatedV(sprite, v3) - getUnInterpolatedV(
                sprite,
                v0
            )) / xyz3.distanceTo(xyz0));
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
            quad.direction(),
            info
        );
        setNormals(newQuad, quad);
        return newQuad;
    }

    public static BlockStateModel generateModel(
        BlockStateModel template,
        UnaryOperator<@Nullable TextureAtlasSprite> spriteSwapper
    ) {
        RandomSource random = RandomSource.create(42L);
        Material.Baked material = template.particleMaterial();
        TextureAtlasSprite swappedParticleSprite = spriteSwapper.apply(material.sprite());
        if (swappedParticleSprite != null) {
            material = new Material.Baked(swappedParticleSprite, material.forceTranslucent());
        }
        List<BlockStateModelPart> parts = new ObjectArrayList<>();
        template.collectParts(random, parts);
        int size = parts.size();
        for (int i = 0; i < size; i++) {
            BlockStateModelPart part = parts.get(i);
            QuadCollection.Builder builder = new QuadCollection.Builder();
            List<BakedQuad> quads;
            for (Direction cullFace : Iterate.directions) {
                quads = part.getQuads(cullFace);
                swapSprites(quads, spriteSwapper).forEach(quad -> builder.addCulledFace(cullFace, quad));
            }
            quads = part.getQuads(null);
            swapSprites(quads, spriteSwapper).forEach(builder::addUnculledFace);
            parts.set(i, new SimpleModelPart(builder.build(), part.useAmbientOcclusion(), material));
        }
        if (size == 1) {
            return new SingleVariant(parts.getFirst());
        }
        return new MultiVariant(parts, material, template.materialFlags());
    }

    public static long calcSpriteUv(long packedUv, TextureAtlasSprite sprite, TextureAtlasSprite newSprite) {
        float u = newSprite.getU(getUnInterpolatedU(sprite, UVPair.unpackU(packedUv)));
        float v = newSprite.getV(getUnInterpolatedV(sprite, UVPair.unpackV(packedUv)));
        return UVPair.pack(u, v);
    }

    public static List<BakedQuad> swapSprites(
        List<BakedQuad> quads,
        UnaryOperator<@Nullable TextureAtlasSprite> spriteSwapper
    ) {
        List<BakedQuad> newQuads = new ArrayList<>(quads);
        int size = quads.size();
        for (int i = 0; i < size; i++) {
            BakedQuad quad = quads.get(i);
            MaterialInfo info = quad.materialInfo();
            TextureAtlasSprite sprite = info.sprite();
            TextureAtlasSprite newSprite = spriteSwapper.apply(sprite);
            if (newSprite == null || sprite == newSprite) {
                continue;
            }

            BakedQuad newQuad = new BakedQuad(
                quad.position0(),
                quad.position1(),
                quad.position2(),
                quad.position3(),
                calcSpriteUv(quad.packedUV0(), sprite, newSprite),
                calcSpriteUv(quad.packedUV1(), sprite, newSprite),
                calcSpriteUv(quad.packedUV2(), sprite, newSprite),
                calcSpriteUv(quad.packedUV3(), sprite, newSprite),
                quad.direction(),
                info
            );
            setNormals(newQuad, quad);
            newQuads.set(i, newQuad);
        }
        return newQuads;
    }

    public static List<BakedQuad> bakeQuads(ModelBaker baker, Identifier id) {
        ResolvedModel model = baker.getModel(id);
        return model.bakeTopGeometry(model.getTopTextureSlots(), baker, BlockModelRotation.IDENTITY).getAll();
    }

    public static List<BakedQuad> replaceQuadLayer(
        List<BakedQuad> quads,
        ChunkSectionLayer layer,
        RenderType itemRenderType
    ) {
        int size = quads.size();
        if (size == 0) {
            return List.of();
        }
        List<BakedQuad> result = new ArrayList<>(size);
        for (BakedQuad quad : quads) {
            MaterialInfo info = quad.materialInfo();
            BakedQuad newQuad = new BakedQuad(
                quad.position0(),
                quad.position1(),
                quad.position2(),
                quad.position3(),
                quad.packedUV0(),
                quad.packedUV1(),
                quad.packedUV2(),
                quad.packedUV3(),
                quad.direction(),
                new MaterialInfo(
                    info.sprite(),
                    layer,
                    itemRenderType,
                    info.tintIndex(),
                    info.shade(),
                    info.lightEmission()
                )
            );
            setNormals(newQuad, quad);
            result.add(newQuad);
        }
        return result;
    }

    public static BakedQuad replaceBakedQuadUV(
        BakedQuad quad,
        long packedUV0,
        long packedUV1,
        long packedUV2,
        long packedUV3,
        MaterialInfo info
    ) {
        BakedQuad newQuad = new BakedQuad(
            quad.position0(),
            quad.position1(),
            quad.position2(),
            quad.position3(),
            packedUV0,
            packedUV1,
            packedUV2,
            packedUV3,
            quad.direction(),
            info
        );
        setNormals(newQuad, quad);
        return newQuad;
    }

    public static void setNormals(BakedQuad quad, Vector3f normal) {
        ((NormalsBakedQuad) (Object) quad).create$setNormal(normal);
    }

    public static void setNormals(BakedQuad quad, BakedQuad target) {
        ((NormalsBakedQuad) (Object) quad).create$setNormal(((NormalsBakedQuad) (Object) target).create$getNormal());
    }
}
