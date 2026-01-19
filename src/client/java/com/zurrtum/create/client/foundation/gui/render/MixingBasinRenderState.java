package com.zurrtum.create.client.foundation.gui.render;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

public record MixingBasinRenderState(Matrix3x2f pose, int x0, int y0, ScreenRectangle bounds) implements PictureInPictureRenderState {
    public MixingBasinRenderState(Matrix3x2f pose, int x, int y) {
        this(pose, x, y, new ScreenRectangle(x, y, 30, 80).transformMaxBounds(pose));
    }

    @Override
    public int x1() {
        return x0 + 30;
    }

    @Override
    public int y1() {
        return y0 + 80;
    }

    @Override
    public float scale() {
        return 23;
    }

    @Override
    public @Nullable ScreenRectangle scissorArea() {
        return null;
    }
}