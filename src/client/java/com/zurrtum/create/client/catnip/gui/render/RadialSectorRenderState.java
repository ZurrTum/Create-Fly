package com.zurrtum.create.client.catnip.gui.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.zurrtum.create.catnip.theme.Color;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

import java.awt.geom.Point2D;
import java.util.List;

import static com.zurrtum.create.Create.MOD_ID;

public record RadialSectorRenderState(
    Matrix3x2f pose, int width, int height, List<Point2D> innerPoints, List<Point2D> outerPoints, int outerRed, int outerGreen, int outerBlue,
    int outerAlpha, int innerRed, int innerGreen, int innerBlue, int innerAlpha, ScreenRect bounds
) implements SimpleGuiElementRenderState {
    public static final RenderPipeline POSITION_COLOR_STRIP = RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
        .withLocation(Identifier.of(MOD_ID, "pipeline/position_color_strip")).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withCull(false)
        .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.TRIANGLE_STRIP).build();

    public RadialSectorRenderState(
        Matrix3x2f pose,
        int width,
        int height,
        List<Point2D> innerPoints,
        List<Point2D> outerPoints,
        Color outerColor,
        Color innerColor
    ) {
        this(
            pose,
            width,
            height,
            innerPoints,
            outerPoints,
            outerColor.getRed(),
            outerColor.getGreen(),
            outerColor.getBlue(),
            outerColor.getAlpha(),
            innerColor.getRed(),
            innerColor.getGreen(),
            innerColor.getBlue(),
            innerColor.getAlpha(),
            new ScreenRect(0, 0, width, height).transformEachVertex(pose)
        );
    }

    @Override
    public RenderPipeline pipeline() {
        return POSITION_COLOR_STRIP;
    }

    @Override
    public void setupVertices(VertexConsumer vertexConsumer, float depth) {
        for (int i = 0; i < innerPoints.size(); i++) {
            Point2D point = outerPoints.get(i);
            vertexConsumer.vertex(pose, (float) point.getX(), (float) point.getY(), 0).color(outerRed, outerGreen, outerBlue, outerAlpha);

            point = innerPoints.get(i);
            vertexConsumer.vertex(pose, (float) point.getX(), (float) point.getY(), 0).color(innerRed, innerGreen, innerBlue, innerAlpha);
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
