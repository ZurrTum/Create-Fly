package com.zurrtum.create.client.catnip.gui.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.catnip.theme.Color;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

import static com.zurrtum.create.client.catnip.render.PonderRenderPipelines.POSITION_COLOR_TRIANGLES;

public record BreadcrumbArrowRenderState(
    Matrix3x2f pose, float x0, float x1, float x2, float x3, float y0, float y1, float y2, int fc1Color, int fc2Color, int fc3Color, int fc4Color,
    ScreenRectangle bounds
) implements GuiElementRenderState {
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
            f1.getRGB(),
            f2.getRGB(),
            f3.getRGB(),
            f4.getRGB(),
            new ScreenRectangle(0, 0, width, height).transformMaxBounds(pose)
        );
    }

    @Override
    public RenderPipeline pipeline() {
        return POSITION_COLOR_TRIANGLES;
    }

    @Override
    public void buildVertices(VertexConsumer vertexConsumer) {
        vertexConsumer.addVertexWith2DPose(pose, x0, y1).setColor(fc1Color);
        vertexConsumer.addVertexWith2DPose(pose, x1, y0).setColor(fc2Color);
        vertexConsumer.addVertexWith2DPose(pose, x1, y1).setColor(fc2Color);
        vertexConsumer.addVertexWith2DPose(pose, x0, y1).setColor(fc1Color);
        vertexConsumer.addVertexWith2DPose(pose, x1, y1).setColor(fc2Color);
        vertexConsumer.addVertexWith2DPose(pose, x1, y2).setColor(fc2Color);
        vertexConsumer.addVertexWith2DPose(pose, x1, y2).setColor(fc2Color);
        vertexConsumer.addVertexWith2DPose(pose, x1, y0).setColor(fc2Color);
        vertexConsumer.addVertexWith2DPose(pose, x2, y0).setColor(fc3Color);
        vertexConsumer.addVertexWith2DPose(pose, x1, y2).setColor(fc2Color);
        vertexConsumer.addVertexWith2DPose(pose, x2, y0).setColor(fc3Color);
        vertexConsumer.addVertexWith2DPose(pose, x2, y2).setColor(fc3Color);
        vertexConsumer.addVertexWith2DPose(pose, x2, y1).setColor(fc3Color);
        vertexConsumer.addVertexWith2DPose(pose, x2, y0).setColor(fc3Color);
        vertexConsumer.addVertexWith2DPose(pose, x3, y0).setColor(fc4Color);
        vertexConsumer.addVertexWith2DPose(pose, x2, y2).setColor(fc3Color);
        vertexConsumer.addVertexWith2DPose(pose, x2, y1).setColor(fc3Color);
        vertexConsumer.addVertexWith2DPose(pose, x3, y2).setColor(fc4Color);
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
