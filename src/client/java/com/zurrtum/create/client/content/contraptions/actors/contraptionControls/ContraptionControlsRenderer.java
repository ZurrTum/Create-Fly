package com.zurrtum.create.client.content.contraptions.actors.contraptionControls;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.contraptions.actors.contraptionControls.ContraptionControlsBlock;
import com.zurrtum.create.content.contraptions.actors.contraptionControls.ContraptionControlsBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ContraptionControlsRenderer extends SmartBlockEntityRenderer<ContraptionControlsBlockEntity, ContraptionControlsRenderer.ContraptionControlsRenderState> {
    public ContraptionControlsRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ContraptionControlsRenderState createRenderState() {
        return new ContraptionControlsRenderState();
    }

    @Override
    public void extractRenderState(
        ContraptionControlsBlockEntity be,
        ContraptionControlsRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        Direction facing = state.blockState.getValue(ContraptionControlsBlock.FACING).getOpposite();
        Vec3 buttonMovementAxis = VecHelper.rotate(new Vec3(0, 1, -.325), AngleHelper.horizontalAngle(facing), Axis.Y);
        state.buttonMovement = buttonMovementAxis.scale(-0.07f + -1 / 24f * be.button.getValue(tickProgress));
        state.buttonOffset = buttonMovementAxis.scale(0.07f);
        state.layer = RenderTypes.solidMovingBlock();
        state.button = CachedBuffers.partialFacing(AllPartialModels.CONTRAPTION_CONTROLS_BUTTON, state.blockState, facing);
        int i = (((int) be.indicator.getValue(tickProgress) / 45) % 8) + 8;
        state.indicator = CachedBuffers.partialFacing(AllPartialModels.CONTRAPTION_CONTROLS_INDICATOR.get(i % 8), state.blockState, facing);
    }

    @Override
    public void submit(ContraptionControlsRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        matrices.pushPose();
        matrices.translate(state.buttonMovement);
        super.submit(state, matrices, queue, cameraState);
        matrices.translate(state.buttonOffset);
        queue.submitCustomGeometry(matrices, state.layer, state::renderButton);
        matrices.popPose();
        queue.submitCustomGeometry(matrices, state.layer, state::renderIndicator);
    }

    public static class ContraptionControlsRenderState extends SmartRenderState {
        public Vec3 buttonMovement;
        public Vec3 buttonOffset;
        public RenderType layer;
        public SuperByteBuffer button;
        public SuperByteBuffer indicator;

        public void renderButton(PoseStack.Pose entry, VertexConsumer vertexConsumer) {
            button.light(lightCoords).renderInto(entry, vertexConsumer);
        }

        public void renderIndicator(PoseStack.Pose entry, VertexConsumer vertexConsumer) {
            indicator.light(lightCoords).renderInto(entry, vertexConsumer);
        }
    }
}
