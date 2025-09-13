package com.zurrtum.create.client.catnip.gui.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

import static com.zurrtum.create.Create.MOD_ID;

public record ArrowRenderState(
    Matrix3x2f pose, float r, float g, float b, float a, float length, ScreenRect bounds
) implements SimpleGuiElementRenderState {
    private static final RenderPipeline TRIANGLE_FAN = RenderPipeline.builder(RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET)
        .withVertexShader("core/position_color").withFragmentShader("core/position_color").withBlend(BlendFunction.TRANSLUCENT)
        .withLocation(Identifier.of(MOD_ID, "pipeline/triangle_fan"))
        .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.TRIANGLE_FAN).build();

    public ArrowRenderState(Matrix3x2f pose, int size, float r, float g, float b, float a, float length) {
        this(pose, r, g, b, a, length, new ScreenRect(0, 0, size, size).transformEachVertex(pose));
    }

    @Override
    public RenderPipeline pipeline() {
        return TRIANGLE_FAN;
    }

    @Override
    public void setupVertices(VertexConsumer vertexConsumer, float depth) {
        vertexConsumer.vertex(pose, 0, -(10 + length), depth).color(r, g, b, a);

        vertexConsumer.vertex(pose, -10, -3, 0).color(r, g, b, 0f);
        vertexConsumer.vertex(pose, -4, -7, 0).color(r, g, b, 0f);
        vertexConsumer.vertex(pose, -0, -8, 0).color(r, g, b, 0f);

        vertexConsumer.vertex(pose, -0, -8, 0).color(r, g, b, 0f);

        vertexConsumer.vertex(pose, 0, -8, 0).color(r, g, b, 0f);
        vertexConsumer.vertex(pose, 4, -7, 0).color(r, g, b, 0f);
        vertexConsumer.vertex(pose, 10, -3, 0).color(r, g, b, 0f);

        vertexConsumer.vertex(pose, 10, -3, 0).color(r, g, b, 0f);
        vertexConsumer.vertex(pose, 10, -3, 0).color(r, g, b, 0f);
        vertexConsumer.vertex(pose, 10, -3, 0).color(r, g, b, 0f);
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
