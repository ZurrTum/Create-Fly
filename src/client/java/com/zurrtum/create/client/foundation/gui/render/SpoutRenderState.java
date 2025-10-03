package com.zurrtum.create.client.foundation.gui.render;

import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import net.minecraft.component.ComponentChanges;
import net.minecraft.fluid.Fluid;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record SpoutRenderState(
    int id, Matrix3x2f pose, Fluid fluid, ComponentChanges components, int x1, int y1, int offset, ScreenRect bounds
) implements SpecialGuiElementRenderState {
    public SpoutRenderState(Matrix3x2f pose, Fluid fluid, ComponentChanges components, int x, int y) {
        this(0, pose, fluid, components, x, y, 0);
    }

    public SpoutRenderState(int id, Matrix3x2f pose, Fluid fluid, ComponentChanges components, int x, int y, int offset) {
        this(id, pose, fluid, components, x, y, offset, new ScreenRect(x, y, 26, 65).transformEachVertex(pose));
    }

    @Override
    public int x2() {
        return x1 + 26;
    }

    @Override
    public int y2() {
        return y1 + 65;
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
