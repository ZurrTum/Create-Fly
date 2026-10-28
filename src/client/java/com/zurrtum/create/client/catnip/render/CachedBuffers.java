package com.zurrtum.create.client.catnip.render;

import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.client.catnip.render.SuperByteBufferCache.Compartment;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import net.minecraft.core.Direction;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Function;

public class CachedBuffers {

    public static final Compartment<BlockState> GENERIC_BLOCK = new Compartment<>();
    public static final Compartment<PartialModel> PARTIAL = new Compartment<>();
    public static final Compartment<Pair<Direction, PartialModel>> DIRECTIONAL_PARTIAL = new Compartment<>();
    public static final Compartment<Pair<Direction, PartialModel>> DIRECTIONAL_PARTIAL_VERTICAL = new Compartment<>();
    public static final Compartment<Pair<Direction, PartialModel>> DIRECTIONAL_PARTIAL_CUSTOM = new Compartment<>();
    private static final Function<Direction, Pose> ROTATE_TO_FACE = Util.memoize(facing -> {
        Pose pose = new Pose();
        switch (facing) {
            case NORTH -> pose.rotateYCentered(Radians.DEG_180);
            case WEST -> pose.rotateYCentered(Radians.DEG_270);
            case EAST -> pose.rotateYCentered(Radians.DEG_90);
            case UP -> pose.rotateXCentered(Radians.DEG_270);
            case DOWN -> pose.rotateXCentered(Radians.DEG_90);
        }
        return pose;
    });
    private static final Function<Direction, Pose> ROTATE_TO_FACE_VERTICAL = Util.memoize(facing -> {
        Pose pose = new Pose();
        switch (facing) {
            case NORTH -> {
                pose.translate(0.5f, 0.5f, 0.5f);
                pose.rotate(Axis.YP, Radians.DEG_180);
                pose.rotate(Axis.XP, Radians.DEG_90);
                pose.translate(-0.5f, -0.5f, -0.5f);
            }
            case WEST -> {
                pose.translate(0.5f, 0.5f, 0.5f);
                pose.rotate(Axis.YP, Radians.DEG_270);
                pose.rotate(Axis.XP, Radians.DEG_90);
                pose.translate(-0.5f, -0.5f, -0.5f);
            }
            case EAST -> {
                pose.translate(0.5f, 0.5f, 0.5f);
                pose.rotate(Axis.YP, Radians.DEG_90);
                pose.rotate(Axis.XP, Radians.DEG_90);
                pose.translate(-0.5f, -0.5f, -0.5f);
            }
            case SOUTH -> pose.rotateXCentered(Radians.DEG_90);
            case DOWN -> pose.rotateXCentered(Radians.DEG_180);
        }
        return pose;
    });

    /**
     * Creates and caches a SuperByteBuffer that has the model of a BlockState baked into it
     *
     * @param toRender the BlockState to be rendered
     * @return the cached SuperByteBuffer
     */
    public static SuperByteBuffer block(BlockState toRender) {
        return block(GENERIC_BLOCK, toRender);
    }

    /**
     * Creates a SuperByteBuffer that has the model of a BlockState baked into it <br />
     * and caches it in the given Compartment
     *
     * @param compartment the Compartment the Buffer should be cached in
     * @param toRender    the BlockState to be rendered
     * @return the cached SuperByteBuffer
     */
    public static SuperByteBuffer block(Compartment<BlockState> compartment, BlockState toRender) {
        return SuperByteBufferCache.getInstance()
            .get(compartment, toRender, () -> SuperBufferFactory.getInstance().createForBlock(toRender));
    }

    public static SuperByteBuffer partial(PartialModel partial, BlockState referenceState) {
        return SuperByteBufferCache.getInstance().get(
            PARTIAL,
            partial,
            () -> SuperBufferFactory.getInstance().createForBlock(partial.get(), referenceState)
        );
    }

    public static SuperByteBuffer partialFacing(PartialModel partial, BlockState referenceState) {
        Direction facing = referenceState.getValue(BlockStateProperties.FACING);
        return partialFacing(partial, referenceState, facing);
    }

    public static SuperByteBuffer partialFacing(PartialModel partial, BlockState referenceState, Direction facing) {
        return SuperByteBufferCache.getInstance().get(
            DIRECTIONAL_PARTIAL,
            Pair.of(facing, partial),
            () -> SuperBufferFactory.getInstance()
                .createForBlock(partial.get(), referenceState, ROTATE_TO_FACE.apply(facing))
        );
    }

    public static SuperByteBuffer partialFacingVertical(
        PartialModel partial,
        BlockState referenceState,
        Direction facing
    ) {
        return SuperByteBufferCache.getInstance().get(
            DIRECTIONAL_PARTIAL_VERTICAL,
            Pair.of(facing, partial),
            () -> SuperBufferFactory.getInstance()
                .createForBlock(partial.get(), referenceState, ROTATE_TO_FACE_VERTICAL.apply(facing))
        );
    }

    /**
     * Creates a SuperByteBuffer for a partial model oriented in a specific direction <br />
     * and caches it in the DIRECTIONAL_PARTIAL_CUSTOM Compartment
     *
     * @param partial        the PartialModel to be rendered
     * @param referenceState the BlockState used as reference for lighting or other properties
     * @param dir            the Direction the partial model is facing
     * @param modelTransform the Function to compute the Pose for the given Direction. <br />
     *                       <b>NOTE:</b> Must remain consistent across calls for the same inputs,
     *                       as an inconsistent transform will result in retrieving incorrect cache results.
     * @return the cached SuperByteBuffer
     */
    public static SuperByteBuffer partialDirectional(
        PartialModel partial,
        BlockState referenceState,
        Direction dir,
        Function<Direction, Pose> modelTransform
    ) {
        return SuperByteBufferCache.getInstance().get(
            DIRECTIONAL_PARTIAL_CUSTOM,
            Pair.of(dir, partial),
            () -> SuperBufferFactory.getInstance()
                .createForBlock(partial.get(), referenceState, modelTransform.apply(dir))
        );
    }

    public static Pose rotateToFace(Direction facing) {
        return ROTATE_TO_FACE.apply(facing);
    }

    public static Pose rotateToFaceVertical(Direction facing) {
        return ROTATE_TO_FACE_VERTICAL.apply(facing);
    }
}
