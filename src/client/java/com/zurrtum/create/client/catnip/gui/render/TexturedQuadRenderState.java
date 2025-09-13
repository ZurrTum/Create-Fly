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

public record TexturedQuadRenderState(
    Matrix3x2f pose, float left, float right, float top, float bot, int red, int green, int blue, int alpha, float u1, float u2, float v1, float v2,
    TextureSetup textureSetup, ScreenRect bounds
) implements SimpleGuiElementRenderState {
    public TexturedQuadRenderState(
        Matrix3x2f pose,
        TextureSetup textureSetup,
        int left,
        int right,
        int top,
        int bot,
        Color color,
        float u1,
        float u2,
        float v1,
        float v2
    ) {
        this(
            pose,
            left,
            right,
            top,
            bot,
            color.getRed(),
            color.getGreen(),
            color.getBlue(),
            color.getAlpha(),
            u1,
            u2,
            v1,
            v2,
            textureSetup,
            new ScreenRect(left, top, right - left, bot - top).transformEachVertex(pose)
        );
    }

    @Override
    public RenderPipeline pipeline() {
        return RenderPipelines.GUI_TEXTURED;
    }

    @Override
    public void setupVertices(VertexConsumer vertexConsumer, float depth) {
        vertexConsumer.vertex(pose, left, bot, depth).color(red, green, blue, alpha).texture(u1, v2);
        vertexConsumer.vertex(pose, right, bot, depth).color(red, green, blue, alpha).texture(u2, v2);
        vertexConsumer.vertex(pose, right, top, depth).color(red, green, blue, alpha).texture(u2, v1);
        vertexConsumer.vertex(pose, left, top, depth).color(red, green, blue, alpha).texture(u1, v1);
    }

    @Override
    public @Nullable ScreenRect scissorArea() {
        return null;
    }
}
