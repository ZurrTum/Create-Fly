package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.foundation.model.BakedModelHelper;
import com.zurrtum.create.content.decoration.copycat.CopycatBlock;
import com.zurrtum.create.content.decoration.copycat.CopycatStepBlock;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Consumer;

public class CopycatStepModel extends CopycatModel {
    protected static final Vec3 VEC_Y_3 = new Vec3(0, .75, 0);
    protected static final Vec3 VEC_Y_2 = new Vec3(0, .5, 0);
    protected static final Vec3 VEC_Y_N2 = new Vec3(0, -.5, 0);
    protected static final AABB CUBE_AABB = new AABB(BlockPos.ZERO);

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
        List<BlockModelPart> parts
    ) {
        Direction facing = state.getValueOrElse(CopycatStepBlock.FACING, Direction.SOUTH);
        boolean upperHalf = state.getValueOrElse(CopycatStepBlock.HALF, Half.BOTTOM) == Half.TOP;
        Vec3 normal = Vec3.atLowerCornerOf(facing.getUnitVec3i());
        Vec3 normalScaled2 = normal.scale(.5);
        Vec3 normalScaledN3 = normal.scale(-.75);
        AABB bb = CUBE_AABB.contract(-normal.x * .75, .75, -normal.z * .75);

        OcclusionData occlusionData = gatherOcclusionData(world, pos, state, material, block);
        BlockStateModel model = getModelOf(material);
        for (BlockModelPart part : getMaterialParts(world, pos, material, random, model)) {
            QuadCollection.Builder builder = new QuadCollection.Builder();
            addCroppedQuads(facing, upperHalf, normalScaled2, normalScaledN3, bb, part.getQuads(null), builder::addUnculledFace);
            for (Direction direction : Iterate.directions) {
                if (occlusionData.isOccluded(direction))
                    continue;
                addCroppedQuads(
                    facing,
                    upperHalf,
                    normalScaled2,
                    normalScaledN3,
                    bb,
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

    protected void addCroppedQuads(
        Direction facing,
        boolean upperHalf,
        Vec3 normalScaled2,
        Vec3 normalScaledN3,
        AABB bb,
        List<BakedQuad> quads,
        Consumer<BakedQuad> consumer
    ) {
        int size = quads.size();
        if (size == 0) {
            return;
        }
        for (boolean top : Iterate.trueAndFalse) {
            for (boolean front : Iterate.trueAndFalse) {
                AABB bb1 = bb;
                if (front)
                    bb1 = bb1.move(normalScaledN3);
                if (top)
                    bb1 = bb1.move(VEC_Y_3);

                Vec3 offset = Vec3.ZERO;
                if (front)
                    offset = offset.add(normalScaled2);
                if (top != upperHalf)
                    offset = offset.add(upperHalf ? VEC_Y_2 : VEC_Y_N2);

                for (int i = 0; i < size; i++) {
                    BakedQuad quad = quads.get(i);
                    Direction direction = quad.direction();

                    if (front && direction == facing)
                        continue;
                    if (!front && direction == facing.getOpposite())
                        continue;
                    if (!top && direction == Direction.UP)
                        continue;
                    if (top && direction == Direction.DOWN)
                        continue;

                    consumer.accept(BakedModelHelper.cropAndMove(quad, bb1, offset));
                }
            }
        }
    }
}
