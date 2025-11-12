package com.zurrtum.create.client.catnip.gui.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.catnip.theme.Color;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import org.joml.Matrix3x2f;

public record TexturedQuadRenderState(
    Matrix3x2f pose, float left, float right, float top, float bot, int red, int green, int blue, int alpha, float u1, float u2, float v1, float v2,
    TextureSetup textureSetup, ScreenRectangle bounds, ScreenRectangle scissorArea
) implements GuiElementRenderState {
    public TexturedQuadRenderState(
        Matrix3x2f pose,
        TextureSetup textureSetup,
        int left,
        int right,
        int top,
        int bot,
        Color color,
        float u1,
        float u2,
        float v1,
        float v2,
        ScreenRectangle scissorArea
    ) {
        this(
            pose,
            left,
            right,
            top,
            bot,
            color.getRed(),
            color.getGreen(),
            color.getBlue(),
            color.getAlpha(),
            u1,
            u2,
            v1,
            v2,
            textureSetup,
            new ScreenRectangle(left, top, right - left, bot - top).transformMaxBounds(pose),
            scissorArea
        );
    }

    @Override
    public RenderPipeline pipeline() {
        return RenderPipelines.GUI_TEXTURED;
    }

    @Override
    public void buildVertices(VertexConsumer vertexConsumer) {
        vertexConsumer.addVertexWith2DPose(pose, left, bot).setColor(red, green, blue, alpha).setUv(u1, v2);
        vertexConsumer.addVertexWith2DPose(pose, right, bot).setColor(red, green, blue, alpha).setUv(u2, v2);
        vertexConsumer.addVertexWith2DPose(pose, right, top).setColor(red, green, blue, alpha).setUv(u2, v1);
        vertexConsumer.addVertexWith2DPose(pose, left, top).setColor(red, green, blue, alpha).setUv(u1, v1);
    }
}
