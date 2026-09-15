package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.catnip.render.SpriteShiftEntry;
import com.zurrtum.create.client.foundation.model.BakedModelHelper;
import com.zurrtum.create.client.foundation.model.SimpleModelPart;
import com.zurrtum.create.content.kinetics.belt.BeltBlock;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity.CasingType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.BakedQuad.MaterialInfo;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BeltModel extends WrapperBlockStateModel {
    private static @Nullable Material.Baked ANDESITE_MATERIAL;

    public BeltModel(BlockState state, UnbakedRoot unbaked) {
        super(state, unbaked);
    }

    private static final SpriteShiftEntry SPRITE_SHIFT = AllSpriteShifts.ANDESIDE_BELT_CASING;

    @Override
    public boolean needUpdateTerrainParticle() {
        return true;
    }

    @Override
    public Material.Baked particleMaterialWithInfo(BlockAndTintGetter world, BlockPos pos, BlockState state) {
        if (world.getBlockEntity(pos) instanceof BeltBlockEntity blockEntity && blockEntity.casing == CasingType.ANDESITE) {
            if (ANDESITE_MATERIAL != null) {
                return ANDESITE_MATERIAL;
            }
            return ANDESITE_MATERIAL = new Material.Baked(AllSpriteShifts.ANDESITE_CASING.getOriginal(), false);
        }
        return model.particleMaterial();
    }

    @Override
    public void addPartsWithInfo(
        BlockAndTintGetter world,
        BlockPos pos,
        BlockState state,
        RandomSource random,
        List<BlockStateModelPart> parts
    ) {
        BeltBlockEntity blockentity = (BeltBlockEntity) world.getBlockEntity(pos);
        if (blockentity == null || blockentity.casing == CasingType.NONE) {
            model.collectParts(random, parts);
            return;
        }
        if (blockentity.casing == CasingType.BRASS) {
            model.collectParts(random, parts);
            if (blockentity.covered) {
                boolean alongX = state.getValue(BeltBlock.HORIZONTAL_FACING).getAxis() == Axis.X;
                (alongX ? AllPartialModels.BRASS_BELT_COVER_X.get() :
                    AllPartialModels.BRASS_BELT_COVER_Z.get()).collectParts(random, parts);
            }
            return;
        }
        TextureAtlasSprite original = SPRITE_SHIFT.getOriginal();
        if (blockentity.covered) {
            boolean alongX = state.getValue(BeltBlock.HORIZONTAL_FACING).getAxis() == Axis.X;
            (alongX ? AllPartialModels.ANDESITE_BELT_COVER_X.get() :
                AllPartialModels.ANDESITE_BELT_COVER_Z.get()).collectParts(random, parts);
        }
        List<BlockStateModelPart> modelParts = new ObjectArrayList<>();
        model.collectParts(random, modelParts);
        for (BlockStateModelPart part : modelParts) {
            parts.add(replaceQuads(original, part));
        }
    }

    private BlockStateModelPart replaceQuads(TextureAtlasSprite replace, BlockStateModelPart part) {
        QuadCollection.Builder builder = new QuadCollection.Builder();
        for (BakedQuad quad : part.getQuads(null)) {
            builder.addUnculledFace(replaceQuad(replace, quad));
        }
        for (Direction direction : Iterate.directions) {
            for (BakedQuad quad : part.getQuads(direction)) {
                builder.addCulledFace(direction, replaceQuad(replace, quad));
            }
        }
        return new SimpleModelPart(builder.build(), part.useAmbientOcclusion(), part.particleMaterial());
    }

    private static long calcSpriteUv(long packedUv) {
        float u = UVPair.unpackU(packedUv);
        float v = UVPair.unpackV(packedUv);
        return UVPair.pack(SPRITE_SHIFT.getTargetU(u), SPRITE_SHIFT.getTargetV(v));
    }

    private BakedQuad replaceQuad(TextureAtlasSprite replace, BakedQuad quad) {
        MaterialInfo info = quad.materialInfo();
        TextureAtlasSprite original = info.sprite();
        if (original != replace) {
            return quad;
        }
        return BakedModelHelper.replaceBakedQuadUV(
            quad,
            calcSpriteUv(quad.packedUV0()),
            calcSpriteUv(quad.packedUV1()),
            calcSpriteUv(quad.packedUV2()),
            calcSpriteUv(quad.packedUV3()),
            info
        );
    }
}
