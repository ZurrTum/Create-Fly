package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.catnip.render.SpriteShiftEntry;
import com.zurrtum.create.client.foundation.model.BakedQuadHelper;
import com.zurrtum.create.content.kinetics.belt.BeltBlock;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity.CasingType;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedGeometry;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.GeometryBakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

import java.util.List;

public class BeltModel extends WrapperBlockStateModel {
    public BeltModel(BlockState state, UnbakedGrouped unbaked) {
        super(state, unbaked);
    }

    private static final SpriteShiftEntry SPRITE_SHIFT = AllSpriteShifts.ANDESIDE_BELT_CASING;

    @Override
    public Sprite particleSpriteWithInfo(BlockRenderView world, BlockPos pos, BlockState state) {
        if (world.getBlockEntity(pos) instanceof BeltBlockEntity blockEntity && blockEntity.casing == CasingType.ANDESITE) {
            return AllSpriteShifts.ANDESITE_CASING.getOriginal();
        } else {
            return model.particleSprite();
        }
    }

    @Override
    public void addPartsWithInfo(BlockRenderView world, BlockPos pos, BlockState state, Random random, List<BlockModelPart> parts) {
        BeltBlockEntity blockentity = (BeltBlockEntity) world.getBlockEntity(pos);
        if (blockentity == null || blockentity.casing == CasingType.NONE) {
            model.addParts(random, parts);
            return;
        }
        if (blockentity.casing == CasingType.BRASS) {
            model.addParts(random, parts);
            if (blockentity.covered) {
                boolean alongX = state.get(BeltBlock.HORIZONTAL_FACING).getAxis() == Axis.X;
                parts.add(alongX ? AllPartialModels.BRASS_BELT_COVER_X.get() : AllPartialModels.BRASS_BELT_COVER_Z.get());
            }
            return;
        }
        Sprite original = SPRITE_SHIFT.getOriginal();
        if (blockentity.covered) {
            boolean alongX = state.get(BeltBlock.HORIZONTAL_FACING).getAxis() == Axis.X;
            parts.add(replaceQuads(original, alongX ? AllPartialModels.ANDESITE_BELT_COVER_X.get() : AllPartialModels.ANDESITE_BELT_COVER_Z.get()));
        }
        for (BlockModelPart part : model.getParts(random)) {
            parts.add(replaceQuads(original, part));
        }
    }

    private BlockModelPart replaceQuads(Sprite replace, BlockModelPart part) {
        BakedGeometry.Builder builder = new BakedGeometry.Builder();
        for (BakedQuad quad : part.getQuads(null)) {
            builder.add(replaceQuad(replace, quad));
        }
        for (Direction direction : Iterate.directions) {
            for (BakedQuad quad : part.getQuads(direction)) {
                builder.add(direction, replaceQuad(replace, quad));
            }
        }
        return new GeometryBakedModel(builder.build(), part.useAmbientOcclusion(), part.particleSprite());
    }

    private BakedQuad replaceQuad(Sprite replace, BakedQuad quad) {
        Sprite original = quad.sprite();
        if (original != replace) {
            return quad;
        }
        BakedQuad newQuad = BakedQuadHelper.clone(quad);
        int[] vertexData = newQuad.vertexData();
        for (int vertex = 0; vertex < 4; vertex++) {
            float u = BakedQuadHelper.getU(vertexData, vertex);
            float v = BakedQuadHelper.getV(vertexData, vertex);
            BakedQuadHelper.setU(vertexData, vertex, SPRITE_SHIFT.getTargetU(u));
            BakedQuadHelper.setV(vertexData, vertex, SPRITE_SHIFT.getTargetV(v));
        }
        return newQuad;
    }
}
