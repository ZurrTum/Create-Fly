package com.zurrtum.create.client.catnip.gui.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record TextureArrowRenderState(
    Matrix3x2f pose, float alpha, float tx, float ty, float tw, float th, TextureSetup textureSetup, ScreenRect bounds
) implements SimpleGuiElementRenderState {
    public TextureArrowRenderState(Matrix3x2f pose, int size, float alpha, TextureSetup textureSetup, float tx, float ty, float tw, float th) {
        this(pose, alpha, tx, ty, tw, th, textureSetup, new ScreenRect(0, 0, size, size).transformEachVertex(pose));
    }

    @Override
    public RenderPipeline pipeline() {
        return RenderPipelines.GUI_TEXTURED;
    }

    @Override
    public void setupVertices(VertexConsumer vertexConsumer) {
        vertexConsumer.vertex(pose, -1, -1).color(1f, 1f, 1f, alpha).texture(tx, ty);
        vertexConsumer.vertex(pose, -1, 1).color(1f, 1f, 1f, alpha).texture(tx, ty + th);
        vertexConsumer.vertex(pose, 1, 1).color(1f, 1f, 1f, alpha).texture(tx + tw, ty + th);
        vertexConsumer.vertex(pose, 1, -1).color(1f, 1f, 1f, alpha).texture(tx + tw, ty);
    }

    @Override
    public @Nullable ScreenRect scissorArea() {
        return null;
    }
}
