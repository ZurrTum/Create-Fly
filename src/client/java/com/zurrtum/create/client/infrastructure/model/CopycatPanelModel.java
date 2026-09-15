package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.foundation.model.BakedModelHelper;
import com.zurrtum.create.client.foundation.model.SimpleModelPart;
import com.zurrtum.create.content.decoration.copycat.CopycatBlock;
import com.zurrtum.create.content.decoration.copycat.CopycatPanelBlock;
import com.zurrtum.create.content.decoration.copycat.CopycatSpecialCases;
import com.zurrtum.create.foundation.block.WrenchableDirectionalBlock;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.BakedQuad.MaterialInfo;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class CopycatPanelModel extends CopycatModel {
    public CopycatPanelModel(BlockState state, UnbakedRoot unbaked) {
        super(state, unbaked);
    }

    @Override
    public void addPartsWithInfo(
        BlockAndTintGetter world,
        BlockPos pos,
        BlockState state,
        CopycatBlock block,
        BlockState material,
        RandomSource random,
        List<BlockStateModelPart> parts
    ) {
        if (CopycatSpecialCases.isTrapdoorMaterial(material)) {
            addModelParts(world, pos, material, random, getModelOf(material), parts);
            return;
        }
        DirectionData directionData = gatherDirectionData(block, state);
        if (CopycatSpecialCases.isBarsMaterial(material)) {
            Direction facing = state.getValueOrElse(CopycatPanelBlock.FACING, Direction.UP);
            BlockState bars = AllBlocks.COPYCAT_BARS.defaultBlockState()
                .setValue(WrenchableDirectionalBlock.FACING, facing);
            BlockStateModel model = getModelOf(material);
            addBarsParts(
                directionData,
                state,
                model.particleMaterial().sprite(),
                getMaterialParts(world, pos, material, random, model),
                getMaterialParts(world, pos, material, random, getModelOf(bars)),
                parts
            );
        } else {
            addPanelParts(
                directionData,
                state,
                getMaterialParts(world, pos, material, random, getModelOf(material)),
                parts
            );
        }
    }

    protected void addBarsParts(
        DirectionData directionData,
        BlockState state,
        TextureAtlasSprite particle,
        List<BlockStateModelPart> material,
        List<BlockStateModelPart> original,
        List<BlockStateModelPart> parts
    ) {
        boolean vertical = state.getValue(CopycatPanelBlock.FACING).getAxis() == Axis.Y;
        TextureAtlasSprite targetSprite = particle;
        Find:
        for (BlockStateModelPart materialPart : material) {
            for (BakedQuad quad : materialPart.getQuads(null)) {
                if (quad.direction() == Direction.UP) {
                    targetSprite = quad.materialInfo().sprite();
                    break Find;
                }
            }
        }
        for (BlockStateModelPart part : original) {
            QuadCollection.Builder builder = new QuadCollection.Builder();
            for (BakedQuad quad : part.getQuads(null)) {
                builder.addUnculledFace(replaceBarsQuad(quad, particle));
            }
            for (Direction direction : Iterate.directions) {
                TextureAtlasSprite sprite = vertical || direction.getAxis() == Axis.Y ? targetSprite : particle;
                if (directionData.isUncull(direction)) {
                    for (BakedQuad quad : part.getQuads(direction)) {
                        builder.addUnculledFace(replaceBarsQuad(quad, sprite));
                    }
                } else {
                    for (BakedQuad quad : part.getQuads(direction)) {
                        builder.addCulledFace(direction, replaceBarsQuad(quad, sprite));
                    }
                }
            }
            parts.add(new SimpleModelPart(builder.build(), part.useAmbientOcclusion(), part.particleMaterial()));
        }
    }

    protected BakedQuad replaceBarsQuad(BakedQuad quad, TextureAtlasSprite targetSprite) {
        MaterialInfo info = quad.materialInfo();
        TextureAtlasSprite original = info.sprite();
        return BakedModelHelper.replaceBakedQuadUV(
            quad,
            BakedModelHelper.calcSpriteUv(quad.packedUV0(), original, targetSprite),
            BakedModelHelper.calcSpriteUv(quad.packedUV1(), original, targetSprite),
            BakedModelHelper.calcSpriteUv(quad.packedUV2(), original, targetSprite),
            BakedModelHelper.calcSpriteUv(quad.packedUV3(), original, targetSprite),
            info
        );
    }

    protected void addPanelParts(
        DirectionData directionData,
        BlockState state,
        List<BlockStateModelPart> original,
        List<BlockStateModelPart> parts
    ) {
        if (original.isEmpty()) {
            return;
        }
        Direction facing = state.getValueOrElse(CopycatPanelBlock.FACING, Direction.UP);
        Direction opposite = facing.getOpposite();
        Vec3 normal = Vec3.atLowerCornerOf(facing.getUnitVec3i());
        Vec3 normalScaled14 = normal.scale(14 / 16.0f);
        AABB front = CUBE_AABB.contract(normal.x * 0.9375d, normal.y * 0.9375d, normal.z * 0.9375d);
        Vec3 frontOffset = Vec3.ZERO;
        AABB back = CUBE_AABB.contract(normal.x * 0.875d, normal.y * 0.875d, normal.z * 0.875d).move(normalScaled14);
        Vec3 backOffset = normal.scale(-0.8125f);
        for (BlockStateModelPart part : original) {
            QuadCollection.Builder builder = new QuadCollection.Builder();
            for (BakedQuad quad : part.getQuads(null)) {
                builder.addUnculledFace(BakedModelHelper.cropAndMove(quad, front, frontOffset));
                builder.addUnculledFace(BakedModelHelper.cropAndMove(quad, back, backOffset));
            }
            for (Direction direction : Iterate.directions) {
                if (directionData.isUncull(direction)) {
                    for (BakedQuad quad : part.getQuads(direction)) {
                        if (direction != facing) {
                            builder.addUnculledFace(BakedModelHelper.cropAndMove(quad, front, frontOffset));
                        }
                        if (direction != opposite) {
                            builder.addUnculledFace(BakedModelHelper.cropAndMove(quad, back, backOffset));
                        }
                    }
                } else {
                    for (BakedQuad quad : part.getQuads(direction)) {
                        if (direction != facing) {
                            builder.addCulledFace(direction, BakedModelHelper.cropAndMove(quad, front, frontOffset));
                        }
                        if (direction != opposite) {
                            builder.addCulledFace(direction, BakedModelHelper.cropAndMove(quad, back, backOffset));
                        }
                    }
                }
            }
            parts.add(new SimpleModelPart(builder.build(), part.useAmbientOcclusion(), part.particleMaterial()));
        }
    }
}
