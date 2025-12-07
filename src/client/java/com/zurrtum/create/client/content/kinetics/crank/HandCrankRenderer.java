package com.zurrtum.create.client.content.kinetics.crank;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.content.kinetics.crank.HandCrankBlock;
import com.zurrtum.create.content.kinetics.crank.HandCrankBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;

public class HandCrankRenderer extends KineticBlockEntityRenderer<HandCrankBlockEntity> {

    public HandCrankRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(HandCrankBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        if (shouldRenderShaft())
            super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

        if (VisualizationManager.supportsVisualization(be.getWorld()))
            return;

        BlockState state = be.getCachedState();
        Direction facing = state.get(Properties.FACING);
        kineticRotationTransform(
            getRenderedHandle(state),
            be,
            facing.getAxis(),
            AngleHelper.rad(getIndependentAngle(be, partialTicks)),
            light
        ).renderInto(ms, buffer.getBuffer(RenderLayer.getSolid()));
    }

    public float getIndependentAngle(HandCrankBlockEntity be, float partialTicks) {
        return getHandCrankIndependentAngle(be, partialTicks);
    }

    /**
     * In degrees
     */
    public static float getHandCrankIndependentAngle(HandCrankBlockEntity be, float partialTicks) {
        return be.independentAngle + partialTicks * be.chasingAngularVelocity;
    }

    public SuperByteBuffer getRenderedHandle(BlockState blockState) {
        Direction facing = blockState.getOrEmpty(HandCrankBlock.FACING).orElse(Direction.UP);
        return CachedBuffers.partialFacing(AllPartialModels.HAND_CRANK_HANDLE, blockState, facing.getOpposite());
    }

    public boolean shouldRenderShaft() {
        return true;
    }
}
