package com.zurrtum.create.client.content.redstone.deskBell;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.redstone.deskBell.DeskBellBlock;
import com.zurrtum.create.content.redstone.deskBell.DeskBellBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class DeskBellRenderer extends SmartBlockEntityRenderer<DeskBellBlockEntity, DeskBellRenderer.DeskBellRenderState> {
    public DeskBellRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public DeskBellRenderState createRenderState() {
        return new DeskBellRenderState();
    }

    @Override
    public void extractRenderState(
        DeskBellBlockEntity be,
        DeskBellRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        float p = be.animation.getValue(tickProgress);
        if (p < 0.004 && !state.blockState.getValueOrElse(DeskBellBlock.POWERED, false)) {
            return;
        }
        state.layer = RenderTypes.solidMovingBlock();
        float f = (float) (1 - 4 * Math.pow((Math.max(p - 0.5, 0)) - 0.5, 2));
        float f2 = (float) (Math.pow(p, 1.25f));
        Direction facing = state.blockState.getValue(DeskBellBlock.FACING);
        state.yRot = Mth.DEG_TO_RAD * AngleHelper.horizontalAngle(facing);
        state.xRot = Mth.DEG_TO_RAD * (AngleHelper.verticalAngle(facing) + 90);
        state.plunger = CachedBuffers.partial(AllPartialModels.DESK_BELL_PLUNGER, state.blockState);
        state.plungerOffset = f * -.75f / 16f;
        state.bell = CachedBuffers.partial(AllPartialModels.DESK_BELL_BELL, state.blockState);
        state.bellOffset = -1 / 16;
        float offset = p * Mth.PI * 4 + be.animationOffset;
        state.bellXRot = Mth.DEG_TO_RAD * (f2 * 8 * Mth.sin(offset));
        state.bellZRot = Mth.DEG_TO_RAD * (f2 * 8 * Mth.cos(offset));
    }

    @Override
    public void submit(DeskBellRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        super.submit(state, matrices, queue, cameraState);
        if (state.layer != null) {
            queue.submitCustomGeometry(matrices, state.layer, state);
        }
    }

    public static class DeskBellRenderState extends SmartRenderState implements SubmitNodeCollector.CustomGeometryRenderer {
        public RenderType layer;
        public float yRot;
        public float xRot;
        public SuperByteBuffer plunger;
        public float plungerOffset;
        public SuperByteBuffer bell;
        public int bellOffset;
        public float bellXRot;
        public float bellZRot;

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            plunger.center().rotateY(yRot).rotateX(xRot).uncenter().translate(0, plungerOffset, 0);
            plunger.light(lightCoords).overlay(OverlayTexture.NO_OVERLAY).renderInto(matricesEntry, vertexConsumer);
            bell.center().rotateY(yRot).rotateX(xRot).translate(0, bellOffset, 0).rotateX(bellXRot).rotateZ(bellZRot).translate(0, -bellOffset, 0);
            bell.scale(0.995f).uncenter().light(lightCoords).overlay(OverlayTexture.NO_OVERLAY).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
