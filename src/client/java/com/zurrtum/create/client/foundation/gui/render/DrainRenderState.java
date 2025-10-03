package com.zurrtum.create.client.foundation.gui.render;

import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import net.minecraft.component.ComponentChanges;
import net.minecraft.fluid.Fluid;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record DrainRenderState(
    Matrix3x2f pose, Fluid fluid, ComponentChanges components, int x1, int y1, ScreenRect bounds
) implements SpecialGuiElementRenderState {
    public DrainRenderState(Matrix3x2f pose, Fluid fluid, ComponentChanges components, int x, int y) {
        this(pose, fluid, components, x, y, new ScreenRect(x, y, 26, 23).transformEachVertex(pose));
    }

    @Override
    public int x2() {
        return x1 + 26;
    }

    @Override
    public int y2() {
        return y1 + 23;
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