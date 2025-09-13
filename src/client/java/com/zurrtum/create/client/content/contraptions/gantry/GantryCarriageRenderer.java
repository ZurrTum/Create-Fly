package com.zurrtum.create.client.content.contraptions.gantry;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.content.contraptions.gantry.GantryCarriageBlock;
import com.zurrtum.create.content.contraptions.gantry.GantryCarriageBlockEntity;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;

public class GantryCarriageRenderer extends KineticBlockEntityRenderer<GantryCarriageBlockEntity> {

    public GantryCarriageRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(
        GantryCarriageBlockEntity be,
        float partialTicks,
        MatrixStack ms,
        VertexConsumerProvider buffer,
        int light,
        int overlay
    ) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

        if (VisualizationManager.supportsVisualization(be.getWorld()))
            return;

        BlockState state = be.getCachedState();
        Direction facing = state.get(GantryCarriageBlock.FACING);
        Boolean alongFirst = state.get(GantryCarriageBlock.AXIS_ALONG_FIRST_COORDINATE);
        Axis rotationAxis = getRotationAxisOf(be);
        BlockPos visualPos = facing.getDirection() == AxisDirection.POSITIVE ? be.getPos() : be.getPos().offset(facing.getOpposite());
        float angleForBE = getAngleForBE(be, visualPos, rotationAxis);

        Axis gantryAxis = Axis.X;
        for (Axis axis : Iterate.axes)
            if (axis != rotationAxis && axis != facing.getAxis())
                gantryAxis = axis;

        if (gantryAxis == Axis.X)
            if (facing == Direction.UP)
                angleForBE *= -1;
        if (gantryAxis == Axis.Y)
            if (facing == Direction.NORTH || facing == Direction.EAST)
                angleForBE *= -1;

        SuperByteBuffer cogs = CachedBuffers.partial(AllPartialModels.GANTRY_COGS, state);
        cogs.center().rotateYDegrees(AngleHelper.horizontalAngle(facing))
            .rotateXDegrees(facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90)
            .rotateYDegrees(alongFirst ^ facing.getAxis() == Axis.X ? 0 : 90).translate(0, -9 / 16f, 0).rotateXDegrees(-angleForBE)
            .translate(0, 9 / 16f, 0).uncenter();

        cogs.light(light).renderInto(ms, buffer.getBuffer(RenderLayer.getSolid()));

    }

    public static float getAngleForBE(KineticBlockEntity be, final BlockPos pos, Axis axis) {
        float time = AnimationTickHolder.getRenderTime(be.getWorld());
        float offset = getRotationOffsetForPosition(be, pos, axis);
        return (time * be.getSpeed() * 3f / 20 + offset) % 360;
    }

    @Override
    protected BlockState getRenderedBlockState(GantryCarriageBlockEntity be) {
        return shaft(getRotationAxisOf(be));
    }

}
