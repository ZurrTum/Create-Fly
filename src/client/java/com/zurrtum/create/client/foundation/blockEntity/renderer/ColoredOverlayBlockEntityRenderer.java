package com.zurrtum.create.client.foundation.blockEntity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public abstract class ColoredOverlayBlockEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T, ColoredOverlayBlockEntityRenderer.ColoredOverlayRenderState> {
    public ColoredOverlayBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public ColoredOverlayRenderState createRenderState() {
        return new ColoredOverlayRenderState();
    }

    @Override
    public void extractRenderState(
        T be,
        ColoredOverlayRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.layer = RenderTypes.solidMovingBlock();
        state.model = getOverlayBuffer(be, state);
        state.color = getColor(be, tickProgress);
    }

    @Override
    public void submit(ColoredOverlayRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        queue.submitCustomGeometry(matrices, state.layer, state);
    }

    protected abstract int getColor(T be, float partialTicks);

    protected abstract SuperByteBuffer getOverlayBuffer(T be, ColoredOverlayRenderState state);

    public static class ColoredOverlayRenderState extends BlockEntityRenderState implements SubmitNodeCollector.CustomGeometryRenderer {
        public RenderType layer;
        public SuperByteBuffer model;
        public int color;

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            model.color(color).light(lightCoords).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
