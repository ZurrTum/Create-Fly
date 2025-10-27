package com.zurrtum.create.client.catnip.gui.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.zurrtum.create.catnip.theme.Color;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

import static com.zurrtum.create.client.catnip.render.PonderRenderPipelines.POSITION_COLOR_TRIANGLES;

public record BreadcrumbArrowRenderState(
    Matrix3x2f pose, float x0, float x1, float x2, float x3, float y0, float y1, float y2, int fc1Color, int fc2Color, int fc3Color, int fc4Color,
    ScreenRect bounds
) implements SimpleGuiElementRenderState {
    public BreadcrumbArrowRenderState(
        Matrix3x2f pose,
        float x0,
        float x1,
        float x2,
        float x3,
        float y0,
        float y1,
        float y2,
        Color f1,
        Color f2,
        Color f3,
        Color f4,
        int width,
        int height
    ) {
        this(
            pose,
            x0,
            x1,
            x2,
            x3,
            y0,
            y1,
            y2,
            f1.getRGB(),
            f2.getRGB(),
            f3.getRGB(),
            f4.getRGB(),
            new ScreenRect(0, 0, width, height).transformEachVertex(pose)
        );
    }

    @Override
    public RenderPipeline pipeline() {
        return POSITION_COLOR_TRIANGLES;
    }

    @Override
    public void setupVertices(VertexConsumer vertexConsumer, float depth) {
        vertexConsumer.vertex(pose, x0, y1, depth).color(fc1Color);
        vertexConsumer.vertex(pose, x1, y0, depth).color(fc2Color);
        vertexConsumer.vertex(pose, x1, y1, depth).color(fc2Color);
        vertexConsumer.vertex(pose, x0, y1, depth).color(fc1Color);
        vertexConsumer.vertex(pose, x1, y1, depth).color(fc2Color);
        vertexConsumer.vertex(pose, x1, y2, depth).color(fc2Color);
        vertexConsumer.vertex(pose, x1, y2, depth).color(fc2Color);
        vertexConsumer.vertex(pose, x1, y0, depth).color(fc2Color);
        vertexConsumer.vertex(pose, x2, y0, depth).color(fc3Color);
        vertexConsumer.vertex(pose, x1, y2, depth).color(fc2Color);
        vertexConsumer.vertex(pose, x2, y0, depth).color(fc3Color);
        vertexConsumer.vertex(pose, x2, y2, depth).color(fc3Color);
        vertexConsumer.vertex(pose, x2, y1, depth).color(fc3Color);
        vertexConsumer.vertex(pose, x2, y0, depth).color(fc3Color);
        vertexConsumer.vertex(pose, x3, y0, depth).color(fc4Color);
        vertexConsumer.vertex(pose, x2, y2, depth).color(fc3Color);
        vertexConsumer.vertex(pose, x2, y1, depth).color(fc3Color);
        vertexConsumer.vertex(pose, x3, y2, depth).color(fc4Color);
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
