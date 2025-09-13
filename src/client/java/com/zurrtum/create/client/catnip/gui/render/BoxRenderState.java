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

public record BoxRenderState(
    Matrix3x2f pose, float x, float y, float width, float height, int f, int c1Red, int c1Green, int c1Blue, int c1Alpha, int c2Red, int c2Green,
    int c2Blue, int c2Alpha, int c3Red, int c3Green, int c3Blue, int c3Alpha, ScreenRect bounds
) implements SimpleGuiElementRenderState {
    public BoxRenderState(Matrix3x2f pose, float x, float y, float width, float height, int f, Color c1, Color c2, Color c3) {
        this(
            pose,
            x,
            y,
            width,
            height,
            f,
            c1.getRed(),
            c1.getGreen(),
            c1.getBlue(),
            c1.getAlpha(),
            c2.getRed(),
            c2.getGreen(),
            c2.getBlue(),
            c2.getAlpha(),
            c3.getRed(),
            c3.getGreen(),
            c3.getBlue(),
            c3.getAlpha(),
            new ScreenRect((int) x, (int) y, (int) width, (int) height).transformEachVertex(pose)
        );
    }

    @Override
    public RenderPipeline pipeline() {
        return RenderPipelines.DEBUG_QUADS;
    }

    @Override
    public void setupVertices(VertexConsumer vertexConsumer, float depth) {
        //outer top
        vertexConsumer.vertex(pose, x - f - 1, y - f - 2, depth).color(c1Red, c1Green, c1Blue, c1Alpha);
        vertexConsumer.vertex(pose, x - f - 1, y - f - 1, depth).color(c1Red, c1Green, c1Blue, c1Alpha);
        vertexConsumer.vertex(pose, x + f + 1 + width, y - f - 1, depth).color(c1Red, c1Green, c1Blue, c1Alpha);
        vertexConsumer.vertex(pose, x + f + 1 + width, y - f - 2, depth).color(c1Red, c1Green, c1Blue, c1Alpha);
        //outer left
        vertexConsumer.vertex(pose, x - f - 2, y - f - 1, depth).color(c1Red, c1Green, c1Blue, c1Alpha);
        vertexConsumer.vertex(pose, x - f - 2, y + f + 1 + height, depth).color(c1Red, c1Green, c1Blue, c1Alpha);
        vertexConsumer.vertex(pose, x - f - 1, y + f + 1 + height, depth).color(c1Red, c1Green, c1Blue, c1Alpha);
        vertexConsumer.vertex(pose, x - f - 1, y - f - 1, depth).color(c1Red, c1Green, c1Blue, c1Alpha);
        //outer bottom
        vertexConsumer.vertex(pose, x - f - 1, y + f + 1 + height, depth).color(c1Red, c1Green, c1Blue, c1Alpha);
        vertexConsumer.vertex(pose, x - f - 1, y + f + 2 + height, depth).color(c1Red, c1Green, c1Blue, c1Alpha);
        vertexConsumer.vertex(pose, x + f + 1 + width, y + f + 2 + height, depth).color(c1Red, c1Green, c1Blue, c1Alpha);
        vertexConsumer.vertex(pose, x + f + 1 + width, y + f + 1 + height, depth).color(c1Red, c1Green, c1Blue, c1Alpha);
        //outer right
        vertexConsumer.vertex(pose, x + f + 1 + width, y - f - 1, depth).color(c1Red, c1Green, c1Blue, c1Alpha);
        vertexConsumer.vertex(pose, x + f + 1 + width, y + f + 1 + height, depth).color(c1Red, c1Green, c1Blue, c1Alpha);
        vertexConsumer.vertex(pose, x + f + 2 + width, y + f + 1 + height, depth).color(c1Red, c1Green, c1Blue, c1Alpha);
        vertexConsumer.vertex(pose, x + f + 2 + width, y - f - 1, depth).color(c1Red, c1Green, c1Blue, c1Alpha);
        //inner background - also render behind the inner edges
        vertexConsumer.vertex(pose, x - f - 1, y - f - 1, depth).color(c1Red, c1Green, c1Blue, c1Alpha);
        vertexConsumer.vertex(pose, x - f - 1, y + f + 1 + height, depth).color(c1Red, c1Green, c1Blue, c1Alpha);
        vertexConsumer.vertex(pose, x + f + 1 + width, y + f + 1 + height, depth).color(c1Red, c1Green, c1Blue, c1Alpha);
        vertexConsumer.vertex(pose, x + f + 1 + width, y - f - 1, depth).color(c1Red, c1Green, c1Blue, c1Alpha);
        //inner top - includes corners
        vertexConsumer.vertex(pose, x - f - 1, y - f - 1, depth).color(c2Red, c2Green, c2Blue, c2Alpha);
        vertexConsumer.vertex(pose, x - f - 1, y - f, depth).color(c2Red, c2Green, c2Blue, c2Alpha);
        vertexConsumer.vertex(pose, x + f + 1 + width, y - f, depth).color(c2Red, c2Green, c2Blue, c2Alpha);
        vertexConsumer.vertex(pose, x + f + 1 + width, y - f - 1, depth).color(c2Red, c2Green, c2Blue, c2Alpha);
        //inner left - excludes corners
        vertexConsumer.vertex(pose, x - f - 1, y - f, depth).color(c2Red, c2Green, c2Blue, c2Alpha);
        vertexConsumer.vertex(pose, x - f - 1, y + f + height, depth).color(c3Red, c3Green, c3Blue, c3Alpha);
        vertexConsumer.vertex(pose, x - f, y + f + height, depth).color(c3Red, c3Green, c3Blue, c3Alpha);
        vertexConsumer.vertex(pose, x - f, y - f, depth).color(c2Red, c2Green, c2Blue, c2Alpha);
        //inner bottom - includes corners
        vertexConsumer.vertex(pose, x - f - 1, y + f + height, depth).color(c3Red, c3Green, c3Blue, c3Alpha);
        vertexConsumer.vertex(pose, x - f - 1, y + f + 1 + height, depth).color(c3Red, c3Green, c3Blue, c3Alpha);
        vertexConsumer.vertex(pose, x + f + 1 + width, y + f + 1 + height, depth).color(c3Red, c3Green, c3Blue, c3Alpha);
        vertexConsumer.vertex(pose, x + f + 1 + width, y + f + height, depth).color(c3Red, c3Green, c3Blue, c3Alpha);
        //inner right - excludes corners
        vertexConsumer.vertex(pose, x + f + width, y - f, depth).color(c2Red, c2Green, c2Blue, c2Alpha);
        vertexConsumer.vertex(pose, x + f + width, y + f + height, depth).color(c3Red, c3Green, c3Blue, c3Alpha);
        vertexConsumer.vertex(pose, x + f + 1 + width, y + f + height, depth).color(c3Red, c3Green, c3Blue, c3Alpha);
        vertexConsumer.vertex(pose, x + f + 1 + width, y - f, depth).color(c2Red, c2Green, c2Blue, c2Alpha);
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
