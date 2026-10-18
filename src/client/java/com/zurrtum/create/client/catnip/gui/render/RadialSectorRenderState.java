package com.zurrtum.create.client.catnip.gui.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.zurrtum.create.catnip.theme.Color;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.world.phys.Vec2;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static com.zurrtum.create.client.catnip.render.PonderRenderPipelines.POSITION_COLOR_STRIP;

public record RadialSectorRenderState(Matrix3x2f pose, List<Vec2> innerPoints, List<Vec2> outerPoints, int outerColor,
                                      int innerColor, ScreenRectangle bounds) implements GuiElementRenderState {
    private static final Vector2f temp = new Vector2f();

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
            new ScreenRectangle((int) minX, (int) minY, (int) (maxX - minX), (int) (maxY - minY)).transformMaxBounds(
                pose)
        );
    }

    @Override
    public RenderPipeline pipeline() {
        return POSITION_COLOR_STRIP;
    }

    @Override
    public void buildVertices(VertexConsumer vertexConsumer) {
        startVertex(vertexConsumer, outerPoints.getFirst(), outerColor);
        addVertex(vertexConsumer, innerPoints.getFirst(), innerColor);
        for (int i = 1; i < innerPoints.size(); i++) {
            addVertex(vertexConsumer, outerPoints.get(i), outerColor);
            addVertex(vertexConsumer, innerPoints.get(i), innerColor);
        }
        endVertex(vertexConsumer);
    }

    private void startVertex(VertexConsumer vertexConsumer, Vec2 point, int color) {
        pose.transformPosition(point.x, point.y, temp);
        vertexConsumer.addVertex(temp.x, temp.y, 0).setColor(-1);
        vertexConsumer.addVertex(temp.x, temp.y, 0).setColor(color);
    }

    private void addVertex(VertexConsumer vertexConsumer, Vec2 point, int color) {
        pose.transformPosition(point.x, point.y, temp);
        vertexConsumer.addVertex(temp.x, temp.y, 0).setColor(color);
    }

    private void endVertex(VertexConsumer vertexConsumer) {
        vertexConsumer.addVertex(temp.x, temp.y, 0).setColor(-1);
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
