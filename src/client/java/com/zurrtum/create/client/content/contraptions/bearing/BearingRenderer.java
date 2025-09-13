package com.zurrtum.create.client.content.contraptions.bearing;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.content.contraptions.bearing.BearingBlock;
import com.zurrtum.create.content.contraptions.bearing.IBearingBlockEntity;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;

public class BearingRenderer<T extends KineticBlockEntity & IBearingBlockEntity> extends KineticBlockEntityRenderer<T> {

    public BearingRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(T be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {

        if (VisualizationManager.supportsVisualization(be.getWorld()))
            return;

        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

        final Direction facing = be.getCachedState().get(Properties.FACING);
        PartialModel top = be.isWoodenTop() ? AllPartialModels.BEARING_TOP_WOODEN : AllPartialModels.BEARING_TOP;
        SuperByteBuffer superBuffer = CachedBuffers.partial(top, be.getCachedState());

        float interpolatedAngle = be.getInterpolatedAngle(partialTicks - 1);
        kineticRotationTransform(superBuffer, be, facing.getAxis(), (float) (interpolatedAngle / 180 * Math.PI), light);

        if (facing.getAxis().isHorizontal())
            superBuffer.rotateCentered(AngleHelper.rad(AngleHelper.horizontalAngle(facing.getOpposite())), Direction.UP);
        superBuffer.rotateCentered(AngleHelper.rad(-90 - AngleHelper.verticalAngle(facing)), Direction.EAST);
        superBuffer.renderInto(ms, buffer.getBuffer(RenderLayer.getSolid()));
    }

    @Override
    protected SuperByteBuffer getRotatedModel(KineticBlockEntity be, BlockState state) {
        return CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state, state.get(BearingBlock.FACING).getOpposite());
    }

}
