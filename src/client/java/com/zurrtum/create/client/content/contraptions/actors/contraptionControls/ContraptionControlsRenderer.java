package com.zurrtum.create.client.content.contraptions.actors.contraptionControls;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.contraptions.actors.contraptionControls.ContraptionControlsBlock;
import com.zurrtum.create.content.contraptions.actors.contraptionControls.ContraptionControlsBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;

public class ContraptionControlsRenderer extends SmartBlockEntityRenderer<ContraptionControlsBlockEntity> {

    public ContraptionControlsRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(
        ContraptionControlsBlockEntity blockEntity,
        float pt,
        MatrixStack ms,
        VertexConsumerProvider buffer,
        int light,
        int overlay
    ) {
        BlockState blockState = blockEntity.getCachedState();
        Direction facing = blockState.get(ContraptionControlsBlock.FACING).getOpposite();
        Vec3d buttonMovementAxis = VecHelper.rotate(new Vec3d(0, 1, -.325), AngleHelper.horizontalAngle(facing), Axis.Y);
        Vec3d buttonMovement = buttonMovementAxis.multiply(-0.07f + -1 / 24f * blockEntity.button.getValue(pt));
        Vec3d buttonOffset = buttonMovementAxis.multiply(0.07f);

        ms.push();
        ms.translate(buttonMovement.x, buttonMovement.y, buttonMovement.z);
        super.renderSafe(blockEntity, pt, ms, buffer, light, overlay);
        ms.translate(buttonOffset.x, buttonOffset.y, buttonOffset.z);

        VertexConsumer vc = buffer.getBuffer(RenderLayer.getSolid());
        CachedBuffers.partialFacing(AllPartialModels.CONTRAPTION_CONTROLS_BUTTON, blockState, facing).light(light).renderInto(ms, vc);

        ms.pop();

        int i = (((int) blockEntity.indicator.getValue(pt) / 45) % 8) + 8;
        CachedBuffers.partialFacing(AllPartialModels.CONTRAPTION_CONTROLS_INDICATOR.get(i % 8), blockState, facing).light(light).renderInto(ms, vc);
    }

}
