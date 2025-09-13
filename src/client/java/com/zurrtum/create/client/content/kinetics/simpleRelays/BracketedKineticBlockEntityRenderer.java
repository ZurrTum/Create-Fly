package com.zurrtum.create.client.content.kinetics.simpleRelays;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.content.kinetics.simpleRelays.BracketedKineticBlockEntity;
import com.zurrtum.create.content.kinetics.simpleRelays.SimpleKineticBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;

public class BracketedKineticBlockEntityRenderer extends KineticBlockEntityRenderer<BracketedKineticBlockEntity> {

    public BracketedKineticBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(
        BracketedKineticBlockEntity be,
        float partialTicks,
        MatrixStack ms,
        VertexConsumerProvider buffer,
        int light,
        int overlay
    ) {

        if (VisualizationManager.supportsVisualization(be.getWorld()))
            return;

        if (!be.getCachedState().isOf(AllBlocks.LARGE_COGWHEEL)) {
            super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
            return;
        }

        // Large cogs sometimes have to offset their teeth by 11.25 degrees in order to
        // mesh properly

        VertexConsumer vc = buffer.getBuffer(RenderLayer.getSolid());
        Axis axis = getRotationAxisOf(be);
        Direction facing = Direction.from(axis, AxisDirection.POSITIVE);
        renderRotatingBuffer(
            be,
            CachedBuffers.partialFacingVertical(AllPartialModels.SHAFTLESS_LARGE_COGWHEEL, be.getCachedState(), facing),
            ms,
            vc,
            light
        );

        float angle = getAngleForLargeCogShaft(be, axis);
        SuperByteBuffer shaft = CachedBuffers.partialFacingVertical(AllPartialModels.COGWHEEL_SHAFT, be.getCachedState(), facing);
        kineticRotationTransform(shaft, be, axis, angle, light);
        shaft.renderInto(ms, vc);

    }

    public static float getAngleForLargeCogShaft(SimpleKineticBlockEntity be, Axis axis) {
        BlockPos pos = be.getPos();
        float offset = getShaftAngleOffset(axis, pos);
        float time = AnimationTickHolder.getRenderTime(be.getWorld());
        return ((time * be.getSpeed() * 3f / 10 + offset) % 360) / 180 * (float) Math.PI;
    }

    public static float getShaftAngleOffset(Axis axis, BlockPos pos) {
        if (KineticBlockEntityVisual.shouldOffset(axis, pos)) {
            return 22.5f;
        } else {
            return 0;
        }
    }

}
