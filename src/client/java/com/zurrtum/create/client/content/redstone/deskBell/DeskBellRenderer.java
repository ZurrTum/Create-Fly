package com.zurrtum.create.client.content.redstone.deskBell;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.redstone.deskBell.DeskBellBlock;
import com.zurrtum.create.content.redstone.deskBell.DeskBellBlockEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class DeskBellRenderer extends SmartBlockEntityRenderer<DeskBellBlockEntity, DeskBellRenderer.DeskBellRenderState> {
    public DeskBellRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public DeskBellRenderState createRenderState() {
        return new DeskBellRenderState();
    }

    @Override
    public void updateRenderState(
        DeskBellBlockEntity be,
        DeskBellRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        super.updateRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        float p = be.animation.getValue(tickProgress);
        if (p < 0.004 && !state.blockState.get(DeskBellBlock.POWERED, false)) {
            return;
        }
        state.layer = RenderLayer.getSolid();
        float f = (float) (1 - 4 * Math.pow((Math.max(p - 0.5, 0)) - 0.5, 2));
        float f2 = (float) (Math.pow(p, 1.25f));
        Direction facing = state.blockState.get(DeskBellBlock.FACING);
        state.yRot = MathHelper.RADIANS_PER_DEGREE * AngleHelper.horizontalAngle(facing);
        state.xRot = MathHelper.RADIANS_PER_DEGREE * (AngleHelper.verticalAngle(facing) + 90);
        state.plunger = CachedBuffers.partial(AllPartialModels.DESK_BELL_PLUNGER, state.blockState);
        state.plungerOffset = f * -.75f / 16f;
        state.bell = CachedBuffers.partial(AllPartialModels.DESK_BELL_BELL, state.blockState);
        state.bellOffset = -1 / 16;
        float offset = p * MathHelper.PI * 4 + be.animationOffset;
        state.bellXRot = MathHelper.RADIANS_PER_DEGREE * (f2 * 8 * MathHelper.sin(offset));
        state.bellZRot = MathHelper.RADIANS_PER_DEGREE * (f2 * 8 * MathHelper.cos(offset));
    }

    @Override
    public void render(DeskBellRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        super.render(state, matrices, queue, cameraState);
        if (state.layer != null) {
            queue.submitCustom(matrices, state.layer, state);
        }
    }

    public static class DeskBellRenderState extends SmartRenderState implements OrderedRenderCommandQueue.Custom {
        public RenderLayer layer;
        public float yRot;
        public float xRot;
        public SuperByteBuffer plunger;
        public float plungerOffset;
        public SuperByteBuffer bell;
        public int bellOffset;
        public float bellXRot;
        public float bellZRot;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            plunger.center().rotateY(yRot).rotateX(xRot).uncenter().translate(0, plungerOffset, 0);
            plunger.light(lightmapCoordinates).overlay(OverlayTexture.DEFAULT_UV).renderInto(matricesEntry, vertexConsumer);
            bell.center().rotateY(yRot).rotateX(xRot).translate(0, bellOffset, 0).rotateX(bellXRot).rotateZ(bellZRot).translate(0, -bellOffset, 0);
            bell.scale(0.995f).uncenter().light(lightmapCoordinates).overlay(OverlayTexture.DEFAULT_UV).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
