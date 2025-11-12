package com.zurrtum.create.client.foundation.gui.render;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record PressRenderState(int id, Matrix3x2f pose, int x0, int y0, int offset, ScreenRectangle bounds) implements PictureInPictureRenderState {
    public PressRenderState(Matrix3x2f pose, int x, int y) {
        this(0, pose, x, y, 0);
    }

    public PressRenderState(int id, Matrix3x2f pose, int x, int y, int offset) {
        this(id, pose, x, y, offset, new ScreenRectangle(x, y, 30, 64).transformMaxBounds(pose));
    }

    @Override
    public int x1() {
        return x0 + 30;
    }

    @Override
    public int y1() {
        return y0 + 64;
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