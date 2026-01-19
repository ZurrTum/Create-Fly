package com.zurrtum.create.client.catnip.gui.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

import java.util.List;

public record BlockTransformRenderState(
    BlockState state, List<BlockModelPart> parts, Matrix3x2f pose, ScreenRectangle bounds, int x0, int y0, int x1, int y1, int padding, float scale,
    float xRot, float yRot, float zRot, @Nullable ScreenRectangle scissorArea
) implements PictureInPictureRenderState {
    public static BlockTransformRenderState create(
        GuiGraphics graphics,
        BlockState block,
        float x,
        float y,
        float scale,
        int padding,
        float xRot,
        float yRot,
        float zRot
    ) {
        float size = scale * 16 + padding;
        Minecraft mc = graphics.minecraft;
        List<BlockModelPart> parts = mc.getBlockRenderer().getBlockModel(block).collectParts(mc.level.getRandom());
        Matrix3x2f pose = new Matrix3x2f(graphics.pose());
        int x1 = (int) x;
        int y1 = (int) y;
        int x2 = (int) (x + size);
        int y2 = (int) (y + size);
        ScreenRectangle bounds = new ScreenRectangle(x1, y1, (int) size, (int) size).transformMaxBounds(pose);
        ScreenRectangle scissor = graphics.scissorStack.peek();
        if (scissor != null) {
            bounds = bounds.intersection(scissor);
        }
        return new BlockTransformRenderState(block, parts, pose, bounds, x1, y1, x2, y2, padding, size, xRot, yRot, zRot, scissor);
    }

    public Object getKey() {
        return List.of(state, scale, padding, xRot, yRot, zRot);
    }

    public static Object getKey(BlockState state, float scale, int padding, float xRot, float yRot, float zRot) {
        return List.of(state, scale * 16 + padding, padding, xRot, yRot, zRot);
    }
}
