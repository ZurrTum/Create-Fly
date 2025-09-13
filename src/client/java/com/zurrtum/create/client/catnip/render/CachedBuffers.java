package com.zurrtum.create.client.catnip.render;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.catnip.render.SuperByteBufferCache.Compartment;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Supplier;

public class CachedBuffers {

    public static final Compartment<BlockState> GENERIC_BLOCK = new Compartment<>();
    public static final Compartment<PartialModel> PARTIAL = new Compartment<>();
    public static final Compartment<Pair<Direction, PartialModel>> DIRECTIONAL_PARTIAL = new Compartment<>();

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
        return SuperByteBufferCache.getInstance().get(compartment, toRender, () -> SuperBufferFactory.getInstance().createForBlock(toRender));
    }

    public static SuperByteBuffer partial(PartialModel partial, BlockState referenceState) {
        return SuperByteBufferCache.getInstance()
            .get(PARTIAL, partial, () -> SuperBufferFactory.getInstance().createForBlock(partial.get(), referenceState));
    }

    public static SuperByteBuffer partial(PartialModel partial, BlockState referenceState, Supplier<MatrixStack> modelTransform) {
        return SuperByteBufferCache.getInstance()
            .get(PARTIAL, partial, () -> SuperBufferFactory.getInstance().createForBlock(partial.get(), referenceState, modelTransform.get()));
    }

    public static SuperByteBuffer partialFacing(PartialModel partial, BlockState referenceState) {
        Direction facing = referenceState.get(Properties.FACING);
        return partialFacing(partial, referenceState, facing);
    }

    public static SuperByteBuffer partialFacing(PartialModel partial, BlockState referenceState, Direction facing) {
        return partialDirectional(partial, referenceState, facing, rotateToFace(facing));
    }

    public static SuperByteBuffer partialFacingVertical(PartialModel partial, BlockState referenceState, Direction facing) {
        return partialDirectional(partial, referenceState, facing, rotateToFaceVertical(facing));
    }

    public static SuperByteBuffer partialDirectional(
        PartialModel partial,
        BlockState referenceState,
        Direction dir,
        Supplier<MatrixStack> modelTransform
    ) {
        return SuperByteBufferCache.getInstance().get(
            DIRECTIONAL_PARTIAL,
            Pair.of(dir, partial),
            () -> SuperBufferFactory.getInstance().createForBlock(partial.get(), referenceState, modelTransform.get())
        );
    }

    public static Supplier<MatrixStack> rotateToFace(Direction facing) {
        return () -> {
            MatrixStack stack = new MatrixStack();
            TransformStack.of(stack).center().rotateYDegrees(AngleHelper.horizontalAngle(facing)).rotateXDegrees(AngleHelper.verticalAngle(facing))
                .uncenter();
            return stack;
        };
    }

    public static Supplier<MatrixStack> rotateToFaceVertical(Direction facing) {
        return () -> {
            MatrixStack stack = new MatrixStack();
            TransformStack.of(stack).center().rotateYDegrees(AngleHelper.horizontalAngle(facing))
                .rotateXDegrees(AngleHelper.verticalAngle(facing) + 90).uncenter();
            return stack;
        };
    }
}
