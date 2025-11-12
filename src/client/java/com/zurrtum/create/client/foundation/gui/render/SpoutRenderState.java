package com.zurrtum.create.client.foundation.gui.render;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record SpoutRenderState(
    int id, Matrix3x2f pose, Fluid fluid, DataComponentPatch components, int x0, int y0, int offset, ScreenRectangle bounds
) implements PictureInPictureRenderState {
    public SpoutRenderState(Matrix3x2f pose, Fluid fluid, DataComponentPatch components, int x, int y) {
        this(0, pose, fluid, components, x, y, 0);
    }

    public SpoutRenderState(int id, Matrix3x2f pose, Fluid fluid, DataComponentPatch components, int x, int y, int offset) {
        this(id, pose, fluid, components, x, y, offset, new ScreenRectangle(x, y, 26, 65).transformMaxBounds(pose));
    }

    @Override
    public int x1() {
        return x0 + 26;
    }

    @Override
    public int y1() {
        return y0 + 65;
    }

    @Override
    public float scale() {
        return 20;
    }

    @Override
    public @Nullable ScreenRectangle scissorArea() {
        return null;
    }
}
