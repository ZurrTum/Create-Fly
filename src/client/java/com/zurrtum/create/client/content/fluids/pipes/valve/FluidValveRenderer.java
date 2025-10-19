package com.zurrtum.create.client.content.fluids.pipes.valve;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.content.fluids.pipes.valve.FluidValveBlock;
import com.zurrtum.create.content.fluids.pipes.valve.FluidValveBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class FluidValveRenderer extends KineticBlockEntityRenderer<FluidValveBlockEntity, FluidValveRenderer.FluidValveRenderState> {
    public FluidValveRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public FluidValveRenderState createRenderState() {
        return new FluidValveRenderState();
    }

    @Override
    public void updateRenderState(
        FluidValveBlockEntity be,
        FluidValveRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        ModelCommandRenderer.@Nullable CrumblingOverlayCommand crumblingOverlay
    ) {
        super.updateRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        if (state.support) {
            return;
        }
        BlockState blockState = be.getCachedState();
        state.pointer = CachedBuffers.partial(AllPartialModels.FLUID_VALVE_POINTER, blockState);
        Direction facing = blockState.get(FluidValveBlock.FACING);
        state.yRot = MathHelper.RADIANS_PER_DEGREE * AngleHelper.horizontalAngle(facing);
        state.xRot = MathHelper.RADIANS_PER_DEGREE * (facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90);
        Axis pipeAxis = FluidValveBlock.getPipeAxis(blockState);
        float pointerRotation = MathHelper.lerp(be.pointer.getValue(tickProgress), 0, -90);
        if (pipeAxis.isHorizontal() && getRotationAxisOf(be) == Axis.X || pipeAxis.isVertical()) {
            state.yRot2 = MathHelper.RADIANS_PER_DEGREE * (90 + pointerRotation);
        } else {
            state.yRot2 = MathHelper.RADIANS_PER_DEGREE * pointerRotation;
        }
    }

    @Override
    protected RenderLayer getRenderType(FluidValveBlockEntity be, BlockState state) {
        return RenderLayer.getSolid();
    }

    @Override
    protected BlockState getRenderedBlockState(FluidValveBlockEntity be) {
        return shaft(getRotationAxisOf(be));
    }

    public static class FluidValveRenderState extends KineticRenderState {
        public SuperByteBuffer pointer;
        public float yRot;
        public float xRot;
        public float yRot2;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            super.render(matricesEntry, vertexConsumer);
            pointer.center().rotateY(yRot).rotateX(xRot).rotateY(yRot2).uncenter();
            pointer.light(lightmapCoordinates).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
