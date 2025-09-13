package com.zurrtum.create.client.content.kinetics.flywheel;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.content.kinetics.flywheel.FlywheelBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

public class FlywheelRenderer extends KineticBlockEntityRenderer<FlywheelBlockEntity> {

    public FlywheelRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(FlywheelBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

        if (VisualizationManager.supportsVisualization(be.getWorld()))
            return;

        BlockState blockState = be.getCachedState();

        float speed = be.visualSpeed.getValue(partialTicks) * 3 / 10f;
        float angle = be.angle + speed * partialTicks;

        VertexConsumer vb = buffer.getBuffer(RenderLayer.getSolid());
        renderFlywheel(be, ms, light, blockState, angle, vb);
    }

    private void renderFlywheel(FlywheelBlockEntity be, MatrixStack ms, int light, BlockState blockState, float angle, VertexConsumer vb) {
        SuperByteBuffer wheel = CachedBuffers.block(blockState);
        kineticRotationTransform(wheel, be, getRotationAxisOf(be), AngleHelper.rad(angle), light);
        wheel.renderInto(ms, vb);
    }

    @Override
    protected BlockState getRenderedBlockState(FlywheelBlockEntity be) {
        return shaft(getRotationAxisOf(be));
    }

}
