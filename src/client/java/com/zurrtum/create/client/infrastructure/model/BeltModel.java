package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.catnip.render.SpriteShiftEntry;
import com.zurrtum.create.client.model.NormalsBakedQuad;
import com.zurrtum.create.content.kinetics.belt.BeltBlock;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity.CasingType;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class BeltModel extends WrapperBlockStateModel {
    public BeltModel(BlockState state, UnbakedRoot unbaked) {
        super(state, unbaked);
    }

    private static final SpriteShiftEntry SPRITE_SHIFT = AllSpriteShifts.ANDESIDE_BELT_CASING;

    @Override
    public TextureAtlasSprite particleSpriteWithInfo(BlockAndTintGetter world, BlockPos pos, BlockState state) {
        if (world.getBlockEntity(pos) instanceof BeltBlockEntity blockEntity && blockEntity.casing == CasingType.ANDESITE) {
            return AllSpriteShifts.ANDESITE_CASING.getOriginal();
        } else {
            return model.particleIcon();
        }
    }

    @Override
    public void addPartsWithInfo(BlockAndTintGetter world, BlockPos pos, BlockState state, RandomSource random, List<BlockModelPart> parts) {
        BeltBlockEntity blockentity = (BeltBlockEntity) world.getBlockEntity(pos);
        if (blockentity == null || blockentity.casing == CasingType.NONE) {
            model.collectParts(random, parts);
            return;
        }
        if (blockentity.casing == CasingType.BRASS) {
            model.collectParts(random, parts);
            if (blockentity.covered) {
                boolean alongX = state.getValue(BeltBlock.HORIZONTAL_FACING).getAxis() == Axis.X;
                parts.add(alongX ? AllPartialModels.BRASS_BELT_COVER_X.get() : AllPartialModels.BRASS_BELT_COVER_Z.get());
            }
            return;
        }
        TextureAtlasSprite original = SPRITE_SHIFT.getOriginal();
        if (blockentity.covered) {
            boolean alongX = state.getValue(BeltBlock.HORIZONTAL_FACING).getAxis() == Axis.X;
            parts.add(replaceQuads(original, alongX ? AllPartialModels.ANDESITE_BELT_COVER_X.get() : AllPartialModels.ANDESITE_BELT_COVER_Z.get()));
        }
        for (BlockModelPart part : model.collectParts(random)) {
            parts.add(replaceQuads(original, part));
        }
    }

    private BlockModelPart replaceQuads(TextureAtlasSprite replace, BlockModelPart part) {
        QuadCollection.Builder builder = new QuadCollection.Builder();
        for (BakedQuad quad : part.getQuads(null)) {
            builder.addUnculledFace(replaceQuad(replace, quad));
        }
        for (Direction direction : Iterate.directions) {
            for (BakedQuad quad : part.getQuads(direction)) {
                builder.addCulledFace(direction, replaceQuad(replace, quad));
            }
        }
        return new SimpleModelWrapper(builder.build(), part.useAmbientOcclusion(), part.particleIcon());
    }

    private static long calcSpriteUv(long packedUv) {
        float u = UVPair.unpackU(packedUv);
        float v = UVPair.unpackV(packedUv);
        return UVPair.pack(SPRITE_SHIFT.getTargetU(u), SPRITE_SHIFT.getTargetV(v));
    }

    private BakedQuad replaceQuad(TextureAtlasSprite replace, BakedQuad quad) {
        TextureAtlasSprite original = quad.sprite();
        if (original != replace) {
            return quad;
        }
        BakedQuad newQuad = new BakedQuad(
            quad.position0(),
            quad.position1(),
            quad.position2(),
            quad.position3(),
            calcSpriteUv(quad.packedUV0()),
            calcSpriteUv(quad.packedUV1()),
            calcSpriteUv(quad.packedUV2()),
            calcSpriteUv(quad.packedUV3()),
            quad.tintIndex(),
            quad.direction(),
            quad.sprite(),
            quad.shade(),
            quad.lightEmission()
        );
        NormalsBakedQuad.setNormals(newQuad, NormalsBakedQuad.getNormals(quad));
        return newQuad;
    }
}
