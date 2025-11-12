package com.zurrtum.create.client.foundation.gui.render;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.content.processing.burner.BlazeBurnerRenderer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public class BlazeBurnerElementRenderer extends PictureInPictureRenderer<BlazeBurnerRenderState> {
    public BlazeBurnerElementRenderer(MultiBufferSource.BufferSource vertexConsumers) {
        super(vertexConsumers);
    }

    @Override
    protected void renderToTexture(BlazeBurnerRenderState state, PoseStack matrices) {
        Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ENTITY_IN_UI);
        matrices.scale(1, 1, -1);
        matrices.mulPose(Axis.XP.rotationDegrees(-22.5f));
        matrices.mulPose(Axis.YP.rotationDegrees(-45));
        matrices.scale(1, -1, 1);
        float horizontalAngle = AngleHelper.rad(270);
        boolean canDrawFlame = state.heatLevel().isAtLeast(HeatLevel.FADING);
        PartialModel drawHat = AllPartialModels.LOGISTICS_HAT;

        VertexConsumer cutout = bufferSource.getBuffer(RenderType.cutoutMipped());
        CachedBuffers.partial(AllPartialModels.BLAZE_CAGE, state.block()).rotateCentered(horizontalAngle + Mth.PI, Direction.UP)
            .light(LightTexture.FULL_BRIGHT).renderInto(matrices.last(), cutout);

        FeatureRenderDispatcher renderDispatcher = Minecraft.getInstance().gameRenderer.getFeatureRenderDispatcher();
        SubmitNodeStorage queue = renderDispatcher.getSubmitNodeStorage();
        BlazeBurnerRenderer.getBlazeBurnerRenderData(
            state.world(),
            state.block(),
            state.heatLevel(),
            state.animation(),
            horizontalAngle,
            canDrawFlame,
            state.drawGoggles(),
            drawHat,
            state.hash()
        ).render(matrices, queue);
        renderDispatcher.renderAllFeatures();
    }

    @Override
    protected float getTranslateY(int height, int windowScaleFactor) {
        return height / 1.6f;
    }

    @Override
    protected String getTextureLabel() {
        return "Blaze Burner";
    }

    @Override
    public Class<BlazeBurnerRenderState> getRenderStateClass() {
        return BlazeBurnerRenderState.class;
    }
}
