package com.zurrtum.create.client.foundation.gui.render;

import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

public record BasinBlazeBurnerRenderState(
    Matrix3x2f pose, int x0, int y0, ScreenRectangle bounds, HeatLevel heat
) implements PictureInPictureRenderState {
    public BasinBlazeBurnerRenderState(Matrix3x2f pose, int x, int y, HeatLevel heat) {
        this(pose, x, y, new ScreenRectangle(x, y, 30, 30).transformMaxBounds(pose), heat);
    }

    @Override
    public int x1() {
        return x0 + 30;
    }

    @Override
    public int y1() {
        return y0 + 36;
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
