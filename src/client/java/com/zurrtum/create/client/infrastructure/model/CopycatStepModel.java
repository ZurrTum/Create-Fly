package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.foundation.model.BakedModelHelper;
import com.zurrtum.create.client.foundation.model.SimpleModelPart;
import com.zurrtum.create.content.decoration.copycat.CopycatBlock;
import com.zurrtum.create.content.decoration.copycat.CopycatStepBlock;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class CopycatStepModel extends CopycatModel {
    protected static final Vec3 VEC_Y_3 = new Vec3(0, 0.75, 0);
    protected static final Vec3 VEC_Y_2 = new Vec3(0, 0.5, 0);
    protected static final Vec3 VEC_Y_N2 = new Vec3(0, -0.5, 0);

    public CopycatStepModel(BlockState state, UnbakedRoot unbaked) {
        super(state, unbaked);
    }

    @Override
    protected void addPartsWithInfo(
        BlockAndTintGetter world,
        BlockPos pos,
        BlockState state,
        CopycatBlock block,
        BlockState material,
        RandomSource random,
        List<BlockStateModelPart> parts
    ) {
        List<BlockStateModelPart> list = getMaterialParts(world, pos, material, random, getModelOf(material));
        if (list.isEmpty()) {
            return;
        }
        Direction facing = state.getValueOrElse(CopycatStepBlock.FACING, Direction.SOUTH);
        Direction opposite = facing.getOpposite();
        boolean upperHalf = state.getValueOrElse(CopycatStepBlock.HALF, Half.BOTTOM) == Half.TOP;
        Vec3 normal = Vec3.atLowerCornerOf(facing.getUnitVec3i());
        Vec3 normalScaled2 = normal.scale(0.5);
        Vec3 normalScaledN3 = normal.scale(-0.75);
        AABB bottomBack = CUBE_AABB.contract(-normal.x * 0.75, 0.75, -normal.z * 0.75);
        AABB bottomFront = bottomBack.move(normalScaledN3);
        AABB topBack = bottomBack.move(VEC_Y_3);
        AABB topFront = bottomFront.move(VEC_Y_3);
        Vec3 topFrontOffset, bottomFrontOffset, topBackOffset, bottomBackOffset;
        if (upperHalf) {
            topFrontOffset = normalScaled2;
            bottomFrontOffset = normalScaled2.add(VEC_Y_2);
            topBackOffset = Vec3.ZERO;
            bottomBackOffset = VEC_Y_2;
        } else {
            topFrontOffset = normalScaled2.add(VEC_Y_N2);
            bottomFrontOffset = normalScaled2;
            topBackOffset = VEC_Y_N2;
            bottomBackOffset = Vec3.ZERO;
        }
        DirectionData directionData = gatherDirectionData(block, state);
        for (BlockStateModelPart part : list) {
            QuadCollection.Builder builder = new QuadCollection.Builder();
            for (BakedQuad quad : part.getQuads(null)) {
                builder.addUnculledFace(BakedModelHelper.cropAndMove(quad, topFront, topFrontOffset));
                builder.addUnculledFace(BakedModelHelper.cropAndMove(quad, bottomFront, bottomFrontOffset));
                builder.addUnculledFace(BakedModelHelper.cropAndMove(quad, topBack, topBackOffset));
                builder.addUnculledFace(BakedModelHelper.cropAndMove(quad, bottomBack, bottomBackOffset));
            }
            for (Direction direction : Iterate.directions) {
                if (directionData.isUncull(direction)) {
                    for (BakedQuad quad : part.getQuads(direction)) {
                        if (direction != facing) {
                            if (direction != Direction.DOWN) {
                                builder.addUnculledFace(BakedModelHelper.cropAndMove(quad, topFront, topFrontOffset));
                            }
                            if (direction != Direction.UP) {
                                builder.addUnculledFace(BakedModelHelper.cropAndMove(
                                    quad,
                                    bottomFront,
                                    bottomFrontOffset
                                ));
                            }
                        }
                        if (direction != opposite) {
                            if (direction != Direction.DOWN) {
                                builder.addUnculledFace(BakedModelHelper.cropAndMove(quad, topBack, topBackOffset));
                            }
                            if (direction != Direction.UP) {
                                builder.addUnculledFace(BakedModelHelper.cropAndMove(
                                    quad,
                                    bottomBack,
                                    bottomBackOffset
                                ));
                            }
                        }
                    }
                } else {
                    for (BakedQuad quad : part.getQuads(direction)) {
                        if (direction != facing) {
                            if (direction != Direction.DOWN) {
                                builder.addCulledFace(
                                    direction,
                                    BakedModelHelper.cropAndMove(quad, topFront, topFrontOffset)
                                );
                            }
                            if (direction != Direction.UP) {
                                builder.addCulledFace(
                                    direction,
                                    BakedModelHelper.cropAndMove(quad, bottomFront, bottomFrontOffset)
                                );
                            }
                        }
                        if (direction != opposite) {
                            if (direction != Direction.DOWN) {
                                builder.addCulledFace(
                                    direction,
                                    BakedModelHelper.cropAndMove(quad, topBack, topBackOffset)
                                );
                            }
                            if (direction != Direction.UP) {
                                builder.addCulledFace(
                                    direction,
                                    BakedModelHelper.cropAndMove(quad, bottomBack, bottomBackOffset)
                                );
                            }
                        }
                    }
                }
            }
            parts.add(new SimpleModelPart(builder.build(), part.useAmbientOcclusion(), part.particleMaterial()));
        }
    }
}
