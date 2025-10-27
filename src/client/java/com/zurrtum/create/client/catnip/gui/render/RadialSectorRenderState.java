package com.zurrtum.create.client.catnip.gui.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.zurrtum.create.catnip.theme.Color;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.util.math.Vec2f;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

import java.util.List;

import static com.zurrtum.create.client.catnip.render.PonderRenderPipelines.POSITION_COLOR_STRIP;

public record RadialSectorRenderState(
    Matrix3x2f pose, List<Vec2f> innerPoints, List<Vec2f> outerPoints, int outerColor, int innerColor, ScreenRect bounds
) implements SimpleGuiElementRenderState {
    public RadialSectorRenderState(
        Matrix3x2f pose,
        double minX,
        double maxX,
        double minY,
        double maxY,
        List<Vec2f> innerPoints,
        List<Vec2f> outerPoints,
        Color innerColor,
        Color outerColor
    ) {
        this(
            pose,
            innerPoints,
            outerPoints,
            outerColor.getRGB(),
            innerColor.getRGB(),
            new ScreenRect((int) minX, (int) minY, (int) (maxX - minX), (int) (maxY - minY)).transformEachVertex(pose)
        );
    }

    @Override
    public RenderPipeline pipeline() {
        return POSITION_COLOR_STRIP;
    }

    @Override
    public void setupVertices(VertexConsumer vertexConsumer, float depth) {
        for (int i = 0; i < innerPoints.size(); i++) {
            Vec2f point = outerPoints.get(i);
            vertexConsumer.vertex(pose, point.x, point.y, depth).color(outerColor);

            point = innerPoints.get(i);
            vertexConsumer.vertex(pose, point.x, point.y, depth).color(innerColor);
        }
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
