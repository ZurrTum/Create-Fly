package com.zurrtum.create.client.ponder.foundation.render;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record TitleTextRenderState(
    Matrix3x2f pose, int x0, int y0, float diff, String title, String otherTitle, ScreenRectangle bounds
) implements PictureInPictureRenderState {
    public TitleTextRenderState(Matrix3x2f pose, int x, int y, float diff, String title, String otherTitle) {
        this(pose, x, y, diff, title, otherTitle, new ScreenRectangle(x, y, 180, 20).transformMaxBounds(pose));
    }

    @Override
    public int x1() {
        return x0 + 180;
    }

    @Override
    public int y1() {
        return y0 + 20;
    }

    @Override
    public float scale() {
        return 1;
    }

    @Override
    public @Nullable ScreenRectangle scissorArea() {
        return null;
    }
}
