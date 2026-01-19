package com.zurrtum.create.client.catnip.gui.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

import static com.zurrtum.create.client.catnip.render.PonderRenderPipelines.TRIANGLE_FAN;

public record DirectionIndicatorRenderState(Matrix3x2f pose, float r, float g, float b, ScreenRectangle bounds) implements GuiElementRenderState {
    public DirectionIndicatorRenderState(Matrix3x2f pose, float r, float g, float b) {
        this(pose, r, g, b, new ScreenRectangle(-5, -5, 10, 5).transformMaxBounds(pose));
    }

    @Override
    public RenderPipeline pipeline() {
        return TRIANGLE_FAN;
    }

    @Override
    public void buildVertices(VertexConsumer vertexConsumer) {
        vertexConsumer.addVertexWith2DPose(pose, 0, 0).setColor(r, g, b, 1);
        vertexConsumer.addVertexWith2DPose(pose, 5, -5).setColor(r, g, b, 0.6f);
        vertexConsumer.addVertexWith2DPose(pose, 3, -4.5f).setColor(r, g, b, 0.7f);
        vertexConsumer.addVertexWith2DPose(pose, 0, -4.2f).setColor(r, g, b, 0.7f);
        vertexConsumer.addVertexWith2DPose(pose, -3, -4.5f).setColor(r, g, b, 0.7f);
        vertexConsumer.addVertexWith2DPose(pose, -5, -5).setColor(r, g, b, 0.6f);
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
