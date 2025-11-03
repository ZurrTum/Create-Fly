package com.zurrtum.create.client.catnip.gui.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

import static com.zurrtum.create.client.catnip.render.PonderRenderPipelines.TRIANGLE_FAN;

public record ArrowRenderState(
    Matrix3x2f pose, float r, float g, float b, float a, float length, ScreenRect bounds
) implements SimpleGuiElementRenderState {
    public ArrowRenderState(Matrix3x2f pose, int size, float r, float g, float b, float a, float length) {
        this(pose, r, g, b, a, length, new ScreenRect(0, 0, size, size).transformEachVertex(pose));
    }

    @Override
    public RenderPipeline pipeline() {
        return TRIANGLE_FAN;
    }

    @Override
    public void setupVertices(VertexConsumer vertexConsumer) {
        vertexConsumer.vertex(pose, 0, -(10 + length)).color(r, g, b, a);
        vertexConsumer.vertex(pose, -9, -3).color(r, g, b, 0f);
        vertexConsumer.vertex(pose, -6, -6).color(r, g, b, 0f);
        vertexConsumer.vertex(pose, -3, -8).color(r, g, b, 0f);
        vertexConsumer.vertex(pose, 0, -8.5f).color(r, g, b, 0f);
        vertexConsumer.vertex(pose, 3, -8).color(r, g, b, 0f);
        vertexConsumer.vertex(pose, 6, -6).color(r, g, b, 0f);
        vertexConsumer.vertex(pose, 9, -3).color(r, g, b, 0f);
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
