package com.zurrtum.create.client.foundation.gui.render;

import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record SandPaperRenderState(Matrix3x2f pose, ItemStack stack, int x1, int y1, ScreenRect bounds) implements SpecialGuiElementRenderState {
    public SandPaperRenderState(Matrix3x2f pose, ItemStack stack, int x, int y) {
        this(pose, stack, x, y, new ScreenRect(x, y, 28, 29).transformEachVertex(pose));
    }

    @Override
    public int x2() {
        return x1 + 28;
    }

    @Override
    public int y2() {
        return y1 + 29;
    }

    @Override
    public float scale() {
        return 32;
    }

    @Override
    public @Nullable ScreenRect scissorArea() {
        return null;
    }
}