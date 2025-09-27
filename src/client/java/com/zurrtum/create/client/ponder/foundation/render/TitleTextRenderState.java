package com.zurrtum.create.client.ponder.foundation.render;

import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record TitleTextRenderState(
    Matrix3x2f pose, int x1, int y1, float diff, String title, String otherTitle, ScreenRect bounds
) implements SpecialGuiElementRenderState {
    public TitleTextRenderState(Matrix3x2f pose, int x, int y, float diff, String title, String otherTitle) {
        this(pose, x, y, diff, title, otherTitle, new ScreenRect(x, y, 180, 20).transformEachVertex(pose));
    }

    @Override
    public int x2() {
        return x1 + 180;
    }

    @Override
    public int y2() {
        return y1 + 20;
    }

    @Override
    public float scale() {
        return 1;
    }

    @Override
    public @Nullable ScreenRect scissorArea() {
        return null;
    }
}
