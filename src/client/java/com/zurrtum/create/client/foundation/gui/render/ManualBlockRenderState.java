package com.zurrtum.create.client.foundation.gui.render;

import net.minecraft.block.BlockState;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record ManualBlockRenderState(Matrix3x2f pose, BlockState state, int x1, int y1, ScreenRect bounds) implements SpecialGuiElementRenderState {
    public ManualBlockRenderState(Matrix3x2f pose, BlockState block, int x, int y) {
        this(pose, block, x, y, new ScreenRect(x, y, 27, 27).transformEachVertex(pose));
    }

    @Override
    public int x2() {
        return x1 + 27;
    }

    @Override
    public int y2() {
        return y1 + 27;
    }

    @Override
    public float scale() {
        return 20;
    }

    @Override
    public @Nullable ScreenRect scissorArea() {
        return null;
    }
}