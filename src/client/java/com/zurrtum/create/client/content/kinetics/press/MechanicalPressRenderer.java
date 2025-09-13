package com.zurrtum.create.client.content.kinetics.press;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.content.kinetics.press.MechanicalPressBlockEntity;
import com.zurrtum.create.content.kinetics.press.PressingBehaviour;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;

public class MechanicalPressRenderer extends KineticBlockEntityRenderer<MechanicalPressBlockEntity> {

    public MechanicalPressRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public boolean rendersOutsideBoundingBox() {
        return true;
    }

    @Override
    protected void renderSafe(
        MechanicalPressBlockEntity be,
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
        PressingBehaviour pressingBehaviour = be.getPressingBehaviour();
        float renderedHeadOffset = pressingBehaviour.getRenderedHeadOffset(partialTicks) * pressingBehaviour.mode.headOffset;

        SuperByteBuffer headRender = CachedBuffers.partialFacing(
            AllPartialModels.MECHANICAL_PRESS_HEAD,
            blockState,
            blockState.get(Properties.HORIZONTAL_FACING)
        );
        headRender.translate(0, -renderedHeadOffset, 0).light(light).renderInto(ms, buffer.getBuffer(RenderLayer.getSolid()));
    }

    @Override
    protected BlockState getRenderedBlockState(MechanicalPressBlockEntity be) {
        return shaft(getRotationAxisOf(be));
    }

}
