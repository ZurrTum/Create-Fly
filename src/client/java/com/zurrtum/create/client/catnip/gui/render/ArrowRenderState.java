package com.zurrtum.create.client.catnip.gui.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

import static com.zurrtum.create.client.catnip.render.PonderRenderPipelines.TRIANGLE_FAN;

public record ArrowRenderState(
    Matrix3x2f pose, float r, float g, float b, float a, float length, ScreenRectangle bounds
) implements GuiElementRenderState {
    public ArrowRenderState(Matrix3x2f pose, int size, float r, float g, float b, float a, float length) {
        this(pose, r, g, b, a, length, new ScreenRectangle(0, 0, size, size).transformMaxBounds(pose));
    }

    @Override
    public RenderPipeline pipeline() {
        return TRIANGLE_FAN;
    }

    @Override
    public void buildVertices(VertexConsumer vertexConsumer) {
        vertexConsumer.addVertexWith2DPose(pose, 0, -(10 + length)).setColor(r, g, b, a);
        vertexConsumer.addVertexWith2DPose(pose, -9, -3).setColor(r, g, b, 0f);
        vertexConsumer.addVertexWith2DPose(pose, -6, -6).setColor(r, g, b, 0f);
        vertexConsumer.addVertexWith2DPose(pose, -3, -8).setColor(r, g, b, 0f);
        vertexConsumer.addVertexWith2DPose(pose, 0, -8.5f).setColor(r, g, b, 0f);
        vertexConsumer.addVertexWith2DPose(pose, 3, -8).setColor(r, g, b, 0f);
        vertexConsumer.addVertexWith2DPose(pose, 6, -6).setColor(r, g, b, 0f);
        vertexConsumer.addVertexWith2DPose(pose, 9, -3).setColor(r, g, b, 0f);
    }

    @Override
    public TextureSetup textureSetup() {
        return TextureSetup.noTexture();
    }

    @Override
    public @Nullable ScreenRectangle scissorArea() {
        return null;
    }
}
