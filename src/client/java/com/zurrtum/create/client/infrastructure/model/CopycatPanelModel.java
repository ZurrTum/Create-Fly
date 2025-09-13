package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.catnip.render.SpriteShiftEntry;
import com.zurrtum.create.client.foundation.model.BakedModelHelper;
import com.zurrtum.create.client.foundation.model.BakedQuadHelper;
import com.zurrtum.create.content.decoration.copycat.CopycatBlock;
import com.zurrtum.create.content.decoration.copycat.CopycatPanelBlock;
import com.zurrtum.create.content.decoration.copycat.CopycatSpecialCases;
import com.zurrtum.create.foundation.block.WrenchableDirectionalBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.*;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

import java.util.List;
import java.util.function.Consumer;

public class CopycatPanelModel extends CopycatModel {
    protected static final Box CUBE_AABB = new Box(BlockPos.ORIGIN);

    public CopycatPanelModel(BlockState state, UnbakedGrouped unbaked) {
        super(state, unbaked);
    }

    @Override
    public void addPartsWithInfo(
        BlockRenderView world,
        BlockPos pos,
        BlockState state,
        CopycatBlock block,
        BlockState material,
        Random random,
        List<BlockModelPart> parts
    ) {
        if (CopycatSpecialCases.isTrapdoorMaterial(material)) {
            addModelParts(world, pos, material, random, getModelOf(material), parts);
            return;
        }
        OcclusionData occlusionData = gatherOcclusionData(world, pos, state, material, block);
        if (CopycatSpecialCases.isBarsMaterial(material)) {
            Direction facing = state.get(CopycatPanelBlock.FACING, Direction.UP);
            BlockState bars = AllBlocks.COPYCAT_BARS.getDefaultState().with(WrenchableDirectionalBlock.FACING, facing);
            BlockStateModel model = getModelOf(material);
            addBarsParts(
                occlusionData,
                state,
                block,
                model.particleSprite(),
                getMaterialParts(world, pos, material, random, model),
                getMaterialParts(world, pos, material, random, getModelOf(bars)),
                parts
            );
        } else {
            addPanelParts(occlusionData, state, block, getMaterialParts(world, pos, material, random, getModelOf(material)), parts);
        }
    }

    protected void addBarsParts(
        OcclusionData occlusionData,
        BlockState state,
        CopycatBlock block,
        Sprite particle,
        List<BlockModelPart> material,
        List<BlockModelPart> original,
        List<BlockModelPart> parts
    ) {
        boolean vertical = state.get(CopycatPanelBlock.FACING).getAxis() == Axis.Y;
        Sprite findSprite = null;
        for (BlockModelPart part : original) {
            BakedGeometry.Builder builder = new BakedGeometry.Builder();
            addBarsCroppedQuads(particle, part.getQuads(null), builder::add);
            for (Direction direction : Iterate.directions) {
                if (occlusionData.isOccluded(direction))
                    continue;
                List<BakedQuad> quads = part.getQuads(direction);
                Sprite targetSprite = particle;
                if (vertical || direction.getAxis() == Axis.Y) {
                    if (findSprite != null) {
                        targetSprite = findSprite;
                    } else {
                        for (BlockModelPart materialPart : material) {
                            for (BakedQuad quad : materialPart.getQuads(null)) {
                                if (quad.face() != Direction.UP)
                                    continue;
                                targetSprite = findSprite = quad.sprite();
                                break;
                            }
                        }
                        if (findSprite == null) {
                            findSprite = particle;
                        }
                    }
                }
                addBarsCroppedQuads(
                    targetSprite,
                    quads,
                    block.shouldFaceAlwaysRender(state, direction) ? builder::add : (BakedQuad quad) -> builder.add(direction, quad)
                );
            }
            parts.add(new GeometryBakedModel(builder.build(), part.useAmbientOcclusion(), part.particleSprite()));
        }
    }

    protected void addBarsCroppedQuads(Sprite targetSprite, List<BakedQuad> quads, Consumer<BakedQuad> consumer) {
        if (targetSprite == null) {
            quads.forEach(consumer);
            return;
        }
        for (BakedQuad quad : quads) {
            Sprite original = quad.sprite();
            BakedQuad newQuad = BakedQuadHelper.clone(quad);
            int[] vertexData = newQuad.vertexData();
            for (int vertex = 0; vertex < 4; vertex++) {
                BakedQuadHelper.setU(
                    vertexData,
                    vertex,
                    targetSprite.getFrameU(SpriteShiftEntry.getUnInterpolatedU(original, BakedQuadHelper.getU(vertexData, vertex)))
                );
                BakedQuadHelper.setV(
                    vertexData,
                    vertex,
                    targetSprite.getFrameV(SpriteShiftEntry.getUnInterpolatedV(original, BakedQuadHelper.getV(vertexData, vertex)))
                );
            }
            consumer.accept(newQuad);
        }
    }

    protected void addPanelParts(
        OcclusionData occlusionData,
        BlockState state,
        CopycatBlock block,
        List<BlockModelPart> original,
        List<BlockModelPart> parts
    ) {
        if (original.isEmpty()) {
            return;
        }
        Direction facing = state.get(CopycatPanelBlock.FACING, Direction.UP);
        Vec3d normal = Vec3d.of(facing.getVector());
        Vec3d normalScaled14 = normal.multiply(14 / 16f);
        Vec3d frontNormalScaledN13 = normal.multiply((float) 0);
        Vec3d normalScaledN13 = normal.multiply(-13 / 16f);
        double frontContract = 15d / 16;
        double contract = 14d / 16;
        Box frontBB = CUBE_AABB.shrink(normal.x * frontContract, normal.y * frontContract, normal.z * frontContract);
        Box bb = CUBE_AABB.shrink(normal.x * contract, normal.y * contract, normal.z * contract).offset(normalScaled14);
        for (BlockModelPart part : original) {
            BakedGeometry.Builder builder = new BakedGeometry.Builder();
            addPanelCroppedQuads(facing, frontBB, bb, frontNormalScaledN13, normalScaledN13, part.getQuads(null), builder::add);
            for (Direction direction : Iterate.directions) {
                if (occlusionData.isOccluded(direction))
                    continue;
                addPanelCroppedQuads(
                    facing,
                    frontBB,
                    bb,
                    frontNormalScaledN13,
                    normalScaledN13,
                    part.getQuads(direction),
                    block.shouldFaceAlwaysRender(state, direction) ? builder::add : (BakedQuad quad) -> builder.add(direction, quad)
                );
            }
            parts.add(new GeometryBakedModel(builder.build(), part.useAmbientOcclusion(), part.particleSprite()));
        }
    }

    protected void addPanelCroppedQuads(
        Direction facing,
        Box frontBB,
        Box bb,
        Vec3d frontNormalScaledN13,
        Vec3d normalScaledN13,
        List<BakedQuad> quads,
        Consumer<BakedQuad> consumer
    ) {
        int size = quads.size();
        if (size == 0) {
            return;
        }
        Box crop;
        Vec3d move;
        for (boolean front : Iterate.trueAndFalse) {
            if (front) {
                crop = frontBB;
                move = frontNormalScaledN13;
            } else {
                crop = bb;
                move = normalScaledN13;
            }
            for (int i = 0; i < size; i++) {
                BakedQuad quad = quads.get(i);
                Direction direction = quad.face();

                if (front && direction == facing)
                    continue;
                if (!front && direction == facing.getOpposite())
                    continue;

                consumer.accept(BakedQuadHelper.cloneWithCustomGeometry(
                    quad,
                    BakedModelHelper.cropAndMove(quad.vertexData(), quad.sprite(), crop, move)
                ));
            }
        }
    }
}