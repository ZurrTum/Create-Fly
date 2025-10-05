package com.zurrtum.create.client.catnip.gui.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.zurrtum.create.catnip.theme.Color;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record GradientRectRenderState(
    Matrix3x2f pose, float left, float top, float right, float bottom, int startRed, int startGreen, int startBlue, int startAlpha, int endRed,
    int endGreen, int endBlue, int endAlpha, ScreenRect bounds
) implements SimpleGuiElementRenderState {
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
            new ScreenRect((int) left, (int) top, (int) (right - left), (int) (bottom - top)).transformEachVertex(pose)
        );
    }

    @Override
    public RenderPipeline pipeline() {
        return RenderPipelines.DEBUG_QUADS;
    }

    @Override
    public void setupVertices(VertexConsumer vertexConsumer) {
        vertexConsumer.vertex(pose, right, top).color(startRed, startGreen, startBlue, startAlpha);
        vertexConsumer.vertex(pose, left, top).color(startRed, startGreen, startBlue, startAlpha);
        vertexConsumer.vertex(pose, left, bottom).color(endRed, endGreen, endBlue, endAlpha);
        vertexConsumer.vertex(pose, right, bottom).color(endRed, endGreen, endBlue, endAlpha);
    }

    @Override
    public TextureSetup textureSetup() {
        return TextureSetup.empty();
    }

    @Override
    public @Nullable ScreenRect scissorArea() {
        return null;
    }
}
