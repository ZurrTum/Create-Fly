package com.zurrtum.create.client.content.fluids.pipes.valve;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.content.fluids.pipes.valve.FluidValveBlock;
import com.zurrtum.create.content.fluids.pipes.valve.FluidValveBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;

public class FluidValveRenderer extends KineticBlockEntityRenderer<FluidValveBlockEntity> {

    public FluidValveRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(FluidValveBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {

        if (VisualizationManager.supportsVisualization(be.getWorld()))
            return;

        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
        BlockState blockState = be.getCachedState();
        SuperByteBuffer pointer = CachedBuffers.partial(AllPartialModels.FLUID_VALVE_POINTER, blockState);
        Direction facing = blockState.get(FluidValveBlock.FACING);

        float pointerRotation = MathHelper.lerp(be.pointer.getValue(partialTicks), 0, -90);
        Axis pipeAxis = FluidValveBlock.getPipeAxis(blockState);
        Axis shaftAxis = getRotationAxisOf(be);

        int pointerRotationOffset = 0;
        if (pipeAxis.isHorizontal() && shaftAxis == Axis.X || pipeAxis.isVertical())
            pointerRotationOffset = 90;

        pointer.center().rotateYDegrees(AngleHelper.horizontalAngle(facing))
            .rotateXDegrees(facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90).rotateYDegrees(pointerRotationOffset + pointerRotation)
            .uncenter().light(light).renderInto(ms, buffer.getBuffer(RenderLayer.getSolid()));
    }

    @Override
    protected BlockState getRenderedBlockState(FluidValveBlockEntity be) {
        return shaft(getRotationAxisOf(be));
    }

}
