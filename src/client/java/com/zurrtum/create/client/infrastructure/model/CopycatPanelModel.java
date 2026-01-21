package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.foundation.model.BakedModelHelper;
import com.zurrtum.create.client.model.NormalsBakedQuad;
import com.zurrtum.create.content.decoration.copycat.CopycatBlock;
import com.zurrtum.create.content.decoration.copycat.CopycatPanelBlock;
import com.zurrtum.create.content.decoration.copycat.CopycatSpecialCases;
import com.zurrtum.create.foundation.block.WrenchableDirectionalBlock;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class CopycatPanelModel extends CopycatModel {
    protected static final AABB CUBE_AABB = new AABB(BlockPos.ZERO);

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
        List<BlockModelPart> parts
    ) {
        if (CopycatSpecialCases.isTrapdoorMaterial(material)) {
            addModelParts(world, pos, material, random, getModelOf(material), parts);
            return;
        }
        OcclusionData occlusionData = gatherOcclusionData(world, pos, state, material, block);
        if (CopycatSpecialCases.isBarsMaterial(material)) {
            Direction facing = state.getValueOrElse(CopycatPanelBlock.FACING, Direction.UP);
            BlockState bars = AllBlocks.COPYCAT_BARS.defaultBlockState().setValue(WrenchableDirectionalBlock.FACING, facing);
            BlockStateModel model = getModelOf(material);
            addBarsParts(
                occlusionData,
                state,
                block,
                model.particleIcon(),
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
        TextureAtlasSprite particle,
        List<BlockModelPart> material,
        List<BlockModelPart> original,
        List<BlockModelPart> parts
    ) {
        boolean vertical = state.getValue(CopycatPanelBlock.FACING).getAxis() == Axis.Y;
        TextureAtlasSprite findSprite = null;
        for (BlockModelPart part : original) {
            QuadCollection.Builder builder = new QuadCollection.Builder();
            addBarsCroppedQuads(particle, part.getQuads(null), builder::addUnculledFace);
            for (Direction direction : Iterate.directions) {
                if (occlusionData.isOccluded(direction))
                    continue;
                List<BakedQuad> quads = part.getQuads(direction);
                TextureAtlasSprite targetSprite = particle;
                if (vertical || direction.getAxis() == Axis.Y) {
                    if (findSprite != null) {
                        targetSprite = findSprite;
                    } else {
                        for (BlockModelPart materialPart : material) {
                            for (BakedQuad quad : materialPart.getQuads(null)) {
                                if (quad.direction() != Direction.UP)
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
                    block.shouldFaceAlwaysRender(state, direction) ? builder::addUnculledFace : (BakedQuad quad) -> builder.addCulledFace(
                        direction,
                        quad
                    )
                );
            }
            parts.add(new SimpleModelWrapper(builder.build(), part.useAmbientOcclusion(), part.particleIcon()));
        }
    }

    protected void addBarsCroppedQuads(@Nullable TextureAtlasSprite targetSprite, List<BakedQuad> quads, Consumer<BakedQuad> consumer) {
        if (targetSprite == null) {
            quads.forEach(consumer);
            return;
        }
        for (BakedQuad quad : quads) {
            TextureAtlasSprite original = quad.sprite();
            BakedQuad newQuad = new BakedQuad(
                quad.position0(),
                quad.position1(),
                quad.position2(),
                quad.position3(),
                BakedModelHelper.calcSpriteUv(quad.packedUV0(), original, targetSprite),
                BakedModelHelper.calcSpriteUv(quad.packedUV1(), original, targetSprite),
                BakedModelHelper.calcSpriteUv(quad.packedUV2(), original, targetSprite),
                BakedModelHelper.calcSpriteUv(quad.packedUV3(), original, targetSprite),
                quad.tintIndex(),
                quad.direction(),
                quad.sprite(),
                quad.shade(),
                quad.lightEmission()
            );
            NormalsBakedQuad.setNormals(newQuad, NormalsBakedQuad.getNormals(quad));
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
        Direction facing = state.getValueOrElse(CopycatPanelBlock.FACING, Direction.UP);
        Vec3 normal = Vec3.atLowerCornerOf(facing.getUnitVec3i());
        Vec3 normalScaled14 = normal.scale(14 / 16f);
        Vec3 frontNormalScaledN13 = normal.scale((float) 0);
        Vec3 normalScaledN13 = normal.scale(-13 / 16f);
        double frontContract = 15d / 16;
        double contract = 14d / 16;
        AABB frontBB = CUBE_AABB.contract(normal.x * frontContract, normal.y * frontContract, normal.z * frontContract);
        AABB bb = CUBE_AABB.contract(normal.x * contract, normal.y * contract, normal.z * contract).move(normalScaled14);
        for (BlockModelPart part : original) {
            QuadCollection.Builder builder = new QuadCollection.Builder();
            addPanelCroppedQuads(facing, frontBB, bb, frontNormalScaledN13, normalScaledN13, part.getQuads(null), builder::addUnculledFace);
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
                    block.shouldFaceAlwaysRender(state, direction) ? builder::addUnculledFace : (BakedQuad quad) -> builder.addCulledFace(
                        direction,
                        quad
                    )
                );
            }
            parts.add(new SimpleModelWrapper(builder.build(), part.useAmbientOcclusion(), part.particleIcon()));
        }
    }

    protected void addPanelCroppedQuads(
        Direction facing,
        AABB frontBB,
        AABB bb,
        Vec3 frontNormalScaledN13,
        Vec3 normalScaledN13,
        List<BakedQuad> quads,
        Consumer<BakedQuad> consumer
    ) {
        int size = quads.size();
        if (size == 0) {
            return;
        }
        AABB crop;
        Vec3 move;
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
                Direction direction = quad.direction();

                if (front && direction == facing)
                    continue;
                if (!front && direction == facing.getOpposite())
                    continue;

                consumer.accept(BakedModelHelper.cropAndMove(quad, crop, move));
            }
        }
    }
}