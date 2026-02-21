package com.zurrtum.create.client.catnip.gui.render;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

public record BlockTransformRenderState(BlockTransformRenderKey key, Matrix3x2f pose, @Nullable ScreenRectangle bounds,
                                        int x0, int y0, int x1, int y1,
                                        @Nullable ScreenRectangle scissorArea) implements PictureInPictureRenderState {
    public static BlockTransformRenderState create(
        GuiGraphics graphics,
        BlockTransformRenderKey key,
        float x,
        float y
    ) {
        Matrix3x2f pose = new Matrix3x2f(graphics.pose());
        float size = key.size;
        int x0 = (int) x;
        int y0 = (int) y;
        int x1 = (int) (x + size);
        int y1 = (int) (y + size);
        ScreenRectangle bounds = new ScreenRectangle(x0, y0, (int) size, (int) size).transformMaxBounds(pose);
        ScreenRectangle scissor = graphics.scissorStack.peek();
        if (scissor != null) {
            bounds = bounds.intersection(scissor);
        }
        return new BlockTransformRenderState(key, pose, bounds, x0, y0, x1, y1, scissor);
    }

    @Override
    public float scale() {
        return 1;
    }
}
