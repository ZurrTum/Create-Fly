package com.zurrtum.create.client.foundation.gui.render;

import net.minecraft.block.BlockState;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record FanRenderState(Matrix3x2f pose, int x1, int y1, BlockState target, ScreenRect bounds) implements SpecialGuiElementRenderState {
    public FanRenderState(Matrix3x2f pose, int x, int y, BlockState target) {
        this(pose, x, y, target, new ScreenRect(x, y, 50, 44).transformEachVertex(pose));
    }

    @Override
    public int x2() {
        return x1 + 50;
    }

    @Override
    public int y2() {
        return y1 + 44;
    }

    @Override
    public float scale() {
        return 24;
    }

    @Override
    public @Nullable ScreenRect scissorArea() {
        return null;
    }
}
