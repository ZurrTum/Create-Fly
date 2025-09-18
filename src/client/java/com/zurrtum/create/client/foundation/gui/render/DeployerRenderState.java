package com.zurrtum.create.client.foundation.gui.render;

import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record DeployerRenderState(int id, Matrix3x2f pose, int x1, int y1, int offset, ScreenRect bounds) implements SpecialGuiElementRenderState {
    public DeployerRenderState(Matrix3x2f pose, int x, int y) {
        this(0, pose, x, y, 0);
    }

    public DeployerRenderState(int id, Matrix3x2f pose, int x, int y, int offset) {
        this(id, pose, x, y, offset, new ScreenRect(x, y, 26, 75).transformEachVertex(pose));
    }

    @Override
    public int x2() {
        return x1 + 26;
    }

    @Override
    public int y2() {
        return y1 + 75;
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
