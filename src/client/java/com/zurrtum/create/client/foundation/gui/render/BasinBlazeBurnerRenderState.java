package com.zurrtum.create.client.foundation.gui.render;

import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record BasinBlazeBurnerRenderState(
    Matrix3x2f pose, int x1, int y1, ScreenRect bounds, HeatLevel heat
) implements SpecialGuiElementRenderState {
    public BasinBlazeBurnerRenderState(Matrix3x2f pose, int x, int y, HeatLevel heat) {
        this(pose, x, y, new ScreenRect(x, y, 30, 30).transformEachVertex(pose), heat);
    }

    @Override
    public int x2() {
        return x1 + 30;
    }

    @Override
    public int y2() {
        return y1 + 36;
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
