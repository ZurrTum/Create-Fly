package com.zurrtum.create.client.catnip.gui.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.catnip.theme.Color;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

public record BoxRenderState(
    Matrix3x2f pose, float x, float y, float width, float height, int f, int c1Red, int c1Green, int c1Blue, int c1Alpha, int c2Red, int c2Green,
    int c2Blue, int c2Alpha, int c3Red, int c3Green, int c3Blue, int c3Alpha, ScreenRectangle bounds
) implements GuiElementRenderState {
    public BoxRenderState(Matrix3x2f pose, float x, float y, float width, float height, int f, Color c1, Color c2, Color c3) {
        this(
            pose,
            x,
            y,
            width,
            height,
            f,
            c1.getRed(),
            c1.getGreen(),
            c1.getBlue(),
            c1.getAlpha(),
            c2.getRed(),
            c2.getGreen(),
            c2.getBlue(),
            c2.getAlpha(),
            c3.getRed(),
            c3.getGreen(),
            c3.getBlue(),
            c3.getAlpha(),
            new ScreenRectangle((int) x, (int) y, (int) width, (int) height).transformMaxBounds(pose)
        );
    }

    @Override
    public RenderPipeline pipeline() {
        return RenderPipelines.DEBUG_QUADS;
    }

    @Override
    public void buildVertices(VertexConsumer vertexConsumer) {
        //outer top
        vertexConsumer.addVertexWith2DPose(pose, x - f - 1, y - f - 2).setColor(c1Red, c1Green, c1Blue, c1Alpha);
        vertexConsumer.addVertexWith2DPose(pose, x - f - 1, y - f - 1).setColor(c1Red, c1Green, c1Blue, c1Alpha);
        vertexConsumer.addVertexWith2DPose(pose, x + f + 1 + width, y - f - 1).setColor(c1Red, c1Green, c1Blue, c1Alpha);
        vertexConsumer.addVertexWith2DPose(pose, x + f + 1 + width, y - f - 2).setColor(c1Red, c1Green, c1Blue, c1Alpha);
        //outer left
        vertexConsumer.addVertexWith2DPose(pose, x - f - 2, y - f - 1).setColor(c1Red, c1Green, c1Blue, c1Alpha);
        vertexConsumer.addVertexWith2DPose(pose, x - f - 2, y + f + 1 + height).setColor(c1Red, c1Green, c1Blue, c1Alpha);
        vertexConsumer.addVertexWith2DPose(pose, x - f - 1, y + f + 1 + height).setColor(c1Red, c1Green, c1Blue, c1Alpha);
        vertexConsumer.addVertexWith2DPose(pose, x - f - 1, y - f - 1).setColor(c1Red, c1Green, c1Blue, c1Alpha);
        //outer bottom
        vertexConsumer.addVertexWith2DPose(pose, x - f - 1, y + f + 1 + height).setColor(c1Red, c1Green, c1Blue, c1Alpha);
        vertexConsumer.addVertexWith2DPose(pose, x - f - 1, y + f + 2 + height).setColor(c1Red, c1Green, c1Blue, c1Alpha);
        vertexConsumer.addVertexWith2DPose(pose, x + f + 1 + width, y + f + 2 + height).setColor(c1Red, c1Green, c1Blue, c1Alpha);
        vertexConsumer.addVertexWith2DPose(pose, x + f + 1 + width, y + f + 1 + height).setColor(c1Red, c1Green, c1Blue, c1Alpha);
        //outer right
        vertexConsumer.addVertexWith2DPose(pose, x + f + 1 + width, y - f - 1).setColor(c1Red, c1Green, c1Blue, c1Alpha);
        vertexConsumer.addVertexWith2DPose(pose, x + f + 1 + width, y + f + 1 + height).setColor(c1Red, c1Green, c1Blue, c1Alpha);
        vertexConsumer.addVertexWith2DPose(pose, x + f + 2 + width, y + f + 1 + height).setColor(c1Red, c1Green, c1Blue, c1Alpha);
        vertexConsumer.addVertexWith2DPose(pose, x + f + 2 + width, y - f - 1).setColor(c1Red, c1Green, c1Blue, c1Alpha);
        //inner background - also render behind the inner edges
        vertexConsumer.addVertexWith2DPose(pose, x - f - 1, y - f - 1).setColor(c1Red, c1Green, c1Blue, c1Alpha);
        vertexConsumer.addVertexWith2DPose(pose, x - f - 1, y + f + 1 + height).setColor(c1Red, c1Green, c1Blue, c1Alpha);
        vertexConsumer.addVertexWith2DPose(pose, x + f + 1 + width, y + f + 1 + height).setColor(c1Red, c1Green, c1Blue, c1Alpha);
        vertexConsumer.addVertexWith2DPose(pose, x + f + 1 + width, y - f - 1).setColor(c1Red, c1Green, c1Blue, c1Alpha);
        //inner top - includes corners
        vertexConsumer.addVertexWith2DPose(pose, x - f - 1, y - f - 1).setColor(c2Red, c2Green, c2Blue, c2Alpha);
        vertexConsumer.addVertexWith2DPose(pose, x - f - 1, y - f).setColor(c2Red, c2Green, c2Blue, c2Alpha);
        vertexConsumer.addVertexWith2DPose(pose, x + f + 1 + width, y - f).setColor(c2Red, c2Green, c2Blue, c2Alpha);
        vertexConsumer.addVertexWith2DPose(pose, x + f + 1 + width, y - f - 1).setColor(c2Red, c2Green, c2Blue, c2Alpha);
        //inner left - excludes corners
        vertexConsumer.addVertexWith2DPose(pose, x - f - 1, y - f).setColor(c2Red, c2Green, c2Blue, c2Alpha);
        vertexConsumer.addVertexWith2DPose(pose, x - f - 1, y + f + height).setColor(c3Red, c3Green, c3Blue, c3Alpha);
        vertexConsumer.addVertexWith2DPose(pose, x - f, y + f + height).setColor(c3Red, c3Green, c3Blue, c3Alpha);
        vertexConsumer.addVertexWith2DPose(pose, x - f, y - f).setColor(c2Red, c2Green, c2Blue, c2Alpha);
        //inner bottom - includes corners
        vertexConsumer.addVertexWith2DPose(pose, x - f - 1, y + f + height).setColor(c3Red, c3Green, c3Blue, c3Alpha);
        vertexConsumer.addVertexWith2DPose(pose, x - f - 1, y + f + 1 + height).setColor(c3Red, c3Green, c3Blue, c3Alpha);
        vertexConsumer.addVertexWith2DPose(pose, x + f + 1 + width, y + f + 1 + height).setColor(c3Red, c3Green, c3Blue, c3Alpha);
        vertexConsumer.addVertexWith2DPose(pose, x + f + 1 + width, y + f + height).setColor(c3Red, c3Green, c3Blue, c3Alpha);
        //inner right - excludes corners
        vertexConsumer.addVertexWith2DPose(pose, x + f + width, y - f).setColor(c2Red, c2Green, c2Blue, c2Alpha);
        vertexConsumer.addVertexWith2DPose(pose, x + f + width, y + f + height).setColor(c3Red, c3Green, c3Blue, c3Alpha);
        vertexConsumer.addVertexWith2DPose(pose, x + f + 1 + width, y + f + height).setColor(c3Red, c3Green, c3Blue, c3Alpha);
        vertexConsumer.addVertexWith2DPose(pose, x + f + 1 + width, y - f).setColor(c2Red, c2Green, c2Blue, c2Alpha);
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
