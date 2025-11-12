package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.catnip.render.SpriteShiftEntry;
import com.zurrtum.create.client.foundation.model.BakedQuadHelper;
import com.zurrtum.create.content.kinetics.belt.BeltBlock;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity.CasingType;
import java.util.List;
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

    private BakedQuad replaceQuad(TextureAtlasSprite replace, BakedQuad quad) {
        TextureAtlasSprite original = quad.sprite();
        if (original != replace) {
            return quad;
        }
        BakedQuad newQuad = BakedQuadHelper.clone(quad);
        int[] vertexData = newQuad.vertices();
        for (int vertex = 0; vertex < 4; vertex++) {
            float u = BakedQuadHelper.getU(vertexData, vertex);
            float v = BakedQuadHelper.getV(vertexData, vertex);
            BakedQuadHelper.setU(vertexData, vertex, SPRITE_SHIFT.getTargetU(u));
            BakedQuadHelper.setV(vertexData, vertex, SPRITE_SHIFT.getTargetV(v));
        }
        return newQuad;
    }
}
