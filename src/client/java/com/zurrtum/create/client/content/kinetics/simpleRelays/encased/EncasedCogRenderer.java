package com.zurrtum.create.client.content.kinetics.simpleRelays.encased;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.content.kinetics.simpleRelays.BracketedKineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.simpleRelays.SimpleKineticBlockEntity;
import com.zurrtum.create.content.kinetics.simpleRelays.encased.EncasedCogwheelBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;

public class EncasedCogRenderer extends KineticBlockEntityRenderer<SimpleKineticBlockEntity> {

    private boolean large;

    public static EncasedCogRenderer small(BlockEntityRendererFactory.Context context) {
        return new EncasedCogRenderer(context, false);
    }

    public static EncasedCogRenderer large(BlockEntityRendererFactory.Context context) {
        return new EncasedCogRenderer(context, true);
    }

    public EncasedCogRenderer(BlockEntityRendererFactory.Context context, boolean large) {
        super(context);
        this.large = large;
    }

    @Override
    protected void renderSafe(
        SimpleKineticBlockEntity be,
        float partialTicks,
        MatrixStack ms,
        VertexConsumerProvider buffer,
        int light,
        int overlay
    ) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
        if (VisualizationManager.supportsVisualization(be.getWorld()))
            return;

        BlockState blockState = be.getCachedState();
        Block block = blockState.getBlock();
        if (!(block instanceof IRotate def))
            return;

        Axis axis = getRotationAxisOf(be);
        BlockPos pos = be.getPos();
        float angle = large ? BracketedKineticBlockEntityRenderer.getAngleForLargeCogShaft(be, axis) : getAngleForBe(be, pos, axis);

        for (Direction d : Iterate.directionsInAxis(getRotationAxisOf(be))) {
            if (!def.hasShaftTowards(be.getWorld(), be.getPos(), blockState, d))
                continue;
            SuperByteBuffer shaft = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, be.getCachedState(), d);
            kineticRotationTransform(shaft, be, axis, angle, light);
            shaft.renderInto(ms, buffer.getBuffer(RenderLayer.getSolid()));
        }
    }

    @Override
    protected SuperByteBuffer getRotatedModel(SimpleKineticBlockEntity be, BlockState state) {
        return CachedBuffers.partialFacingVertical(
            large ? AllPartialModels.SHAFTLESS_LARGE_COGWHEEL : AllPartialModels.SHAFTLESS_COGWHEEL,
            state,
            Direction.from(state.get(EncasedCogwheelBlock.AXIS), AxisDirection.POSITIVE)
        );
    }

}
