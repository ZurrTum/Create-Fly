package com.zurrtum.create.client.content.kinetics.transmission;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.transmission.SplitShaftBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;

public class SplitShaftRenderer extends KineticBlockEntityRenderer<SplitShaftBlockEntity> {

    public SplitShaftRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(SplitShaftBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        if (VisualizationManager.supportsVisualization(be.getWorld()))
            return;

        Block block = be.getCachedState().getBlock();
        final Axis boxAxis = ((IRotate) block).getRotationAxis(be.getCachedState());
        final BlockPos pos = be.getPos();
        float time = AnimationTickHolder.getRenderTime(be.getWorld());

        for (Direction direction : Iterate.directions) {
            Axis axis = direction.getAxis();
            if (boxAxis != axis)
                continue;

            float offset = getRotationOffsetForPosition(be, pos, axis);
            float angle = (time * be.getSpeed() * 3f / 10) % 360;
            float modifier = be.getRotationSpeedModifier(direction);

            angle *= modifier;
            angle += offset;
            angle = angle / 180f * (float) Math.PI;

            SuperByteBuffer superByteBuffer = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, be.getCachedState(), direction);
            kineticRotationTransform(superByteBuffer, be, axis, angle, light);
            superByteBuffer.renderInto(ms, buffer.getBuffer(RenderLayer.getSolid()));
        }
    }

}
