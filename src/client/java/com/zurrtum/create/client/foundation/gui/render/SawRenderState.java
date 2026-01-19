package com.zurrtum.create.client.foundation.gui.render;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

public record SawRenderState(Matrix3x2f pose, int x0, int y0, ScreenRectangle bounds) implements PictureInPictureRenderState {
    public SawRenderState(Matrix3x2f pose, int x, int y) {
        this(pose, x, y, new ScreenRectangle(x, y, 33, 33).transformMaxBounds(pose));
    }

    @Override
    public int x1() {
        return x0 + 33;
    }

    @Override
    public int y1() {
        return y0 + 33;
    }

    @Override
    public float scale() {
        return 25;
    }

    @Override
    public @Nullable ScreenRectangle scissorArea() {
        return null;
    }
}
