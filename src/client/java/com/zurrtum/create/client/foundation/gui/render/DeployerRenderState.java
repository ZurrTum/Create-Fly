package com.zurrtum.create.client.foundation.gui.render;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record DeployerRenderState(
    int id, Matrix3x2f pose, int x0, int y0, int offset, ScreenRectangle bounds
) implements PictureInPictureRenderState {
    public DeployerRenderState(Matrix3x2f pose, int x, int y) {
        this(0, pose, x, y, 0);
    }

    public DeployerRenderState(int id, Matrix3x2f pose, int x, int y, int offset) {
        this(id, pose, x, y, offset, new ScreenRectangle(x, y, 26, 75).transformMaxBounds(pose));
    }

    @Override
    public int x1() {
        return x0 + 26;
    }

    @Override
    public int y1() {
        return y0 + 75;
    }

    @Override
    public float scale() {
        return 20;
    }

    @Override
    public @Nullable ScreenRectangle scissorArea() {
        return null;
    }
}
