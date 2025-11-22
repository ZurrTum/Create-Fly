package com.zurrtum.create.client.content.contraptions.actors.contraptionControls;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.contraptions.actors.contraptionControls.ContraptionControlsBlock;
import com.zurrtum.create.content.contraptions.actors.contraptionControls.ContraptionControlsBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class ContraptionControlsRenderer extends SmartBlockEntityRenderer<ContraptionControlsBlockEntity, ContraptionControlsRenderer.ContraptionControlsRenderState> {
    public ContraptionControlsRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public ContraptionControlsRenderState createRenderState() {
        return new ContraptionControlsRenderState();
    }

    @Override
    public void updateRenderState(
        ContraptionControlsBlockEntity be,
        ContraptionControlsRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        super.updateRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        Direction facing = state.blockState.get(ContraptionControlsBlock.FACING).getOpposite();
        Vec3d buttonMovementAxis = VecHelper.rotate(new Vec3d(0, 1, -.325), AngleHelper.horizontalAngle(facing), Axis.Y);
        state.buttonMovement = buttonMovementAxis.multiply(-0.07f + -1 / 24f * be.button.getValue(tickProgress));
        state.buttonOffset = buttonMovementAxis.multiply(0.07f);
        state.layer = RenderLayer.getSolid();
        state.button = CachedBuffers.partialFacing(AllPartialModels.CONTRAPTION_CONTROLS_BUTTON, state.blockState, facing);
        int i = (((int) be.indicator.getValue(tickProgress) / 45) % 8) + 8;
        state.indicator = CachedBuffers.partialFacing(AllPartialModels.CONTRAPTION_CONTROLS_INDICATOR.get(i % 8), state.blockState, facing);
    }

    @Override
    public void render(ContraptionControlsRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        matrices.push();
        matrices.translate(state.buttonMovement);
        super.render(state, matrices, queue, cameraState);
        matrices.translate(state.buttonOffset);
        queue.submitCustom(matrices, state.layer, state::renderButton);
        matrices.pop();
        queue.submitCustom(matrices, state.layer, state::renderIndicator);
    }

    public static class ContraptionControlsRenderState extends SmartRenderState {
        public Vec3d buttonMovement;
        public Vec3d buttonOffset;
        public RenderLayer layer;
        public SuperByteBuffer button;
        public SuperByteBuffer indicator;

        public void renderButton(MatrixStack.Entry entry, VertexConsumer vertexConsumer) {
            button.light(lightmapCoordinates).renderInto(entry, vertexConsumer);
        }

        public void renderIndicator(MatrixStack.Entry entry, VertexConsumer vertexConsumer) {
            indicator.light(lightmapCoordinates).renderInto(entry, vertexConsumer);
        }
    }
}
