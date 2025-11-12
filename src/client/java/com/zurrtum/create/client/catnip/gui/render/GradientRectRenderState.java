package com.zurrtum.create.client.catnip.gui.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.catnip.theme.Color;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record GradientRectRenderState(
    Matrix3x2f pose, float left, float top, float right, float bottom, int startRed, int startGreen, int startBlue, int startAlpha, int endRed,
    int endGreen, int endBlue, int endAlpha, ScreenRectangle bounds
) implements GuiElementRenderState {
    public GradientRectRenderState(Matrix3x2f pose, float left, float top, float right, float bottom, Color startColor, Color endColor) {
        this(
            pose,
            left,
            top,
            right,
            bottom,
            startColor.getRed(),
            startColor.getGreen(),
            startColor.getBlue(),
            startColor.getAlpha(),
            endColor.getRed(),
            endColor.getGreen(),
            endColor.getBlue(),
            endColor.getAlpha(),
            new ScreenRectangle((int) left, (int) top, (int) (right - left), (int) (bottom - top)).transformMaxBounds(pose)
        );
    }

    @Override
    public RenderPipeline pipeline() {
        return RenderPipelines.DEBUG_QUADS;
    }

    @Override
    public void buildVertices(VertexConsumer vertexConsumer) {
        vertexConsumer.addVertexWith2DPose(pose, right, top).setColor(startRed, startGreen, startBlue, startAlpha);
        vertexConsumer.addVertexWith2DPose(pose, left, top).setColor(startRed, startGreen, startBlue, startAlpha);
        vertexConsumer.addVertexWith2DPose(pose, left, bottom).setColor(endRed, endGreen, endBlue, endAlpha);
        vertexConsumer.addVertexWith2DPose(pose, right, bottom).setColor(endRed, endGreen, endBlue, endAlpha);
    }

    @Override
    public TextureSetup textureSetup() {
        return TextureSetup.noTexture();
    }

    @Override
    public @Nullable ScreenRectangle scissorArea() {
        return null;
    }
}
