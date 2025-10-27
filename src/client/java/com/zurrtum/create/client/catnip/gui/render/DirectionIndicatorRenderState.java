package com.zurrtum.create.client.catnip.gui.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

import static com.zurrtum.create.client.catnip.render.PonderRenderPipelines.TRIANGLE_FAN;

public record DirectionIndicatorRenderState(Matrix3x2f pose, float r, float g, float b, ScreenRect bounds) implements SimpleGuiElementRenderState {
    public DirectionIndicatorRenderState(Matrix3x2f pose, float r, float g, float b) {
        this(pose, r, g, b, new ScreenRect(-5, -5, 10, 5).transformEachVertex(pose));
    }

    @Override
    public RenderPipeline pipeline() {
        return TRIANGLE_FAN;
    }

    @Override
    public void setupVertices(VertexConsumer vertexConsumer, float depth) {
        vertexConsumer.vertex(pose, 0, 0, depth).color(r, g, b, 1);
        vertexConsumer.vertex(pose, 5, -5, depth).color(r, g, b, 0.6f);
        vertexConsumer.vertex(pose, 3, -4.5f, depth).color(r, g, b, 0.7f);
        vertexConsumer.vertex(pose, 0, -4.2f, depth).color(r, g, b, 0.7f);
        vertexConsumer.vertex(pose, -3, -4.5f, depth).color(r, g, b, 0.7f);
        vertexConsumer.vertex(pose, -5, -5, depth).color(r, g, b, 0.6f);
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
