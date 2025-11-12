package com.zurrtum.create.client.catnip.gui.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.catnip.theme.Color;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

import java.util.List;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.world.phys.Vec2;

import static com.zurrtum.create.client.catnip.render.PonderRenderPipelines.POSITION_COLOR_STRIP;

public record RadialSectorRenderState(
    Matrix3x2f pose, List<Vec2> innerPoints, List<Vec2> outerPoints, int outerColor, int innerColor, ScreenRectangle bounds
) implements GuiElementRenderState {
    public RadialSectorRenderState(
        Matrix3x2f pose,
        double minX,
        double maxX,
        double minY,
        double maxY,
        List<Vec2> innerPoints,
        List<Vec2> outerPoints,
        Color innerColor,
        Color outerColor
    ) {
        this(
            pose,
            innerPoints,
            outerPoints,
            outerColor.getRGB(),
            innerColor.getRGB(),
            new ScreenRectangle((int) minX, (int) minY, (int) (maxX - minX), (int) (maxY - minY)).transformMaxBounds(pose)
        );
    }

    @Override
    public RenderPipeline pipeline() {
        return POSITION_COLOR_STRIP;
    }

    @Override
    public void buildVertices(VertexConsumer vertexConsumer) {
        for (int i = 0; i < innerPoints.size(); i++) {
            Vec2 point = outerPoints.get(i);
            vertexConsumer.addVertexWith2DPose(pose, point.x, point.y).setColor(outerColor);

            point = innerPoints.get(i);
            vertexConsumer.addVertexWith2DPose(pose, point.x, point.y).setColor(innerColor);
        }
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
