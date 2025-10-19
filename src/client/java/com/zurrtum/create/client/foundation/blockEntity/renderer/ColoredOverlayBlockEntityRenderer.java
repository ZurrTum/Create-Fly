package com.zurrtum.create.client.foundation.blockEntity.renderer;

import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public abstract class ColoredOverlayBlockEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T, ColoredOverlayBlockEntityRenderer.ColoredOverlayRenderState> {
    public ColoredOverlayBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public ColoredOverlayRenderState createRenderState() {
        return new ColoredOverlayRenderState();
    }

    @Override
    public void updateRenderState(
        T be,
        ColoredOverlayRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        BlockEntityRenderState.updateBlockEntityRenderState(be, state, crumblingOverlay);
        state.layer = RenderLayer.getSolid();
        state.model = getOverlayBuffer(be, state);
        state.color = getColor(be, tickProgress);
    }

    @Override
    public void render(ColoredOverlayRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        queue.submitCustom(matrices, state.layer, state);
    }

    protected abstract int getColor(T be, float partialTicks);

    protected abstract SuperByteBuffer getOverlayBuffer(T be, ColoredOverlayRenderState state);

    public static class ColoredOverlayRenderState extends BlockEntityRenderState implements OrderedRenderCommandQueue.Custom {
        public RenderLayer layer;
        public SuperByteBuffer model;
        public int color;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            model.color(color).light(lightmapCoordinates).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
