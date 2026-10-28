package com.zurrtum.create.client.foundation.gui.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.content.processing.burner.BlazeBurnerRenderer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.LightCoordsUtil;

public class BlazeBurnerElementRenderer extends GuiBlockRenderer<BlazeBurnerRenderState> {
    @Override
    protected void renderToTexture(BlazeBurnerRenderState state, PoseStack matrices, SubmitNodeCollector queue) {
        Pose pose = matrices.last();
        pose.scale(1, 1, -1);
        pose.rotate(Axis.XP, Radians.DEG_337P5);
        pose.rotate(Axis.YP, Radians.DEG_315);
        pose.scale(1, -1, 1);
        boolean canDrawFlame = state.heatLevel().isAtLeast(HeatLevel.FADING);
        PartialModel drawHat = AllPartialModels.LOGISTICS_HAT;

        matrices.pushPose();
        matrices.rotateYCentered(Radians.DEG_90);
        CachedBuffers.partial(AllPartialModels.BLAZE_CAGE, state.block()).light(LightCoordsUtil.FULL_BRIGHT)
            .submit(matrices, queue);
        matrices.popPose();
        BlazeBurnerRenderer.getBlazeBurnerRenderData(
            null,
            state.block(),
            state.heatLevel(),
            state.animation(),
            Radians.DEG_270,
            canDrawFlame,
            state.drawGoggles(),
            drawHat,
            state.hash()
        ).submit(matrices, queue);
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
