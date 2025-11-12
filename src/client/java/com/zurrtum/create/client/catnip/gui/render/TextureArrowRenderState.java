package com.zurrtum.create.client.catnip.gui.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record TextureArrowRenderState(
    Matrix3x2f pose, float alpha, float tx, float ty, float tw, float th, TextureSetup textureSetup, ScreenRectangle bounds
) implements GuiElementRenderState {
    public TextureArrowRenderState(Matrix3x2f pose, int size, float alpha, TextureSetup textureSetup, float tx, float ty, float tw, float th) {
        this(pose, alpha, tx, ty, tw, th, textureSetup, new ScreenRectangle(0, 0, size, size).transformMaxBounds(pose));
    }

    @Override
    public RenderPipeline pipeline() {
        return RenderPipelines.GUI_TEXTURED;
    }

    @Override
    public void buildVertices(VertexConsumer vertexConsumer) {
        vertexConsumer.addVertexWith2DPose(pose, -1, -1).setColor(1f, 1f, 1f, alpha).setUv(tx, ty);
        vertexConsumer.addVertexWith2DPose(pose, -1, 1).setColor(1f, 1f, 1f, alpha).setUv(tx, ty + th);
        vertexConsumer.addVertexWith2DPose(pose, 1, 1).setColor(1f, 1f, 1f, alpha).setUv(tx + tw, ty + th);
        vertexConsumer.addVertexWith2DPose(pose, 1, -1).setColor(1f, 1f, 1f, alpha).setUv(tx + tw, ty);
    }

    @Override
    public @Nullable ScreenRectangle scissorArea() {
        return null;
    }
}
