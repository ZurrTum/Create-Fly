package com.zurrtum.create.client.foundation.gui.render;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record CrushWheelRenderState(Matrix3x2f pose, int x0, int y0, ScreenRectangle bounds) implements PictureInPictureRenderState {
    public CrushWheelRenderState(Matrix3x2f pose, int x, int y) {
        this(pose, x, y, new ScreenRectangle(x, y, 92, 48).transformMaxBounds(pose));
    }

    @Override
    public int x1() {
        return x0 + 92;
    }

    @Override
    public int y1() {
        return y0 + 48;
    }

    @Override
    public float scale() {
        return 22;
    }

    @Override
    public @Nullable ScreenRectangle scissorArea() {
        return null;
    }
}
