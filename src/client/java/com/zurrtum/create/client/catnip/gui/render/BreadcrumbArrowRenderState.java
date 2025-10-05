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

public record BreadcrumbArrowRenderState(
    Matrix3x2f pose, float x0, float x1, float x2, float x3, float y0, float y1, float y2, int fc1Red, int fc1Green, int fc1Blue, int fc1Alpha,
    int fc2Red, int fc2Green, int fc2Blue, int fc2Alpha, int fc3Red, int fc3Green, int fc3Blue, int fc3Alpha, int fc4Red, int fc4Green, int fc4Blue,
    int fc4Alpha, ScreenRect bounds
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
            f1.getRed(),
            f1.getGreen(),
            f1.getBlue(),
            f1.getAlpha(),
            f2.getRed(),
            f2.getGreen(),
            f2.getBlue(),
            f2.getAlpha(),
            f3.getRed(),
            f3.getGreen(),
            f3.getBlue(),
            f3.getAlpha(),
            f4.getRed(),
            f4.getGreen(),
            f4.getBlue(),
            f4.getAlpha(),
            new ScreenRect(0, 0, width, height).transformEachVertex(pose)
        );
    }

    @Override
    public RenderPipeline pipeline() {
        return RenderPipelines.DEBUG_STRUCTURE_QUADS;
    }

    @Override
    public void setupVertices(VertexConsumer vertexConsumer) {
        vertexConsumer.vertex(pose, x1, y0).color(fc2Red, fc2Green, fc2Blue, fc2Alpha);
        vertexConsumer.vertex(pose, x0, y1).color(fc1Red, fc1Green, fc1Blue, fc1Alpha);
        vertexConsumer.vertex(pose, x1, y2).color(fc2Red, fc2Green, fc2Blue, fc2Alpha);
        vertexConsumer.vertex(pose, x2, y0).color(fc3Red, fc3Green, fc3Blue, fc3Alpha);
        vertexConsumer.vertex(pose, x2, y0).color(fc3Red, fc3Green, fc3Blue, fc3Alpha);
        vertexConsumer.vertex(pose, x1, y2).color(fc2Red, fc2Green, fc2Blue, fc2Alpha);
        vertexConsumer.vertex(pose, x2, y1).color(fc3Red, fc3Green, fc3Blue, fc3Alpha);
        vertexConsumer.vertex(pose, x3, y0).color(fc4Red, fc4Green, fc4Blue, fc4Alpha);
        vertexConsumer.vertex(pose, x2, y1).color(fc3Red, fc3Green, fc3Blue, fc3Alpha);
        vertexConsumer.vertex(pose, x1, y2).color(fc2Red, fc2Green, fc2Blue, fc2Alpha);
        vertexConsumer.vertex(pose, x2, y2).color(fc3Red, fc3Green, fc3Blue, fc3Alpha);
        vertexConsumer.vertex(pose, x3, y2).color(fc4Red, fc4Green, fc4Blue, fc4Alpha);
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
