package com.zurrtum.create.client.foundation.gui.render;

import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record PressRenderState(Matrix3x2f pose, int x1, int y1, ScreenRect bounds) implements SpecialGuiElementRenderState {
    public PressRenderState(Matrix3x2f pose, int x, int y) {
        this(pose, x, y, new ScreenRect(x, y, 30, 64).transformEachVertex(pose));
    }

    @Override
    public int x2() {
        return x1 + 30;
    }

    @Override
    public int y2() {
        return y1 + 64;
    }

    @Override
    public float scale() {
        return 23;
    }

    @Override
    public @Nullable ScreenRect scissorArea() {
        return null;
    }
}