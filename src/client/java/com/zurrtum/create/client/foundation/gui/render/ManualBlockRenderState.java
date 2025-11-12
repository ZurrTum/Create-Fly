package com.zurrtum.create.client.foundation.gui.render;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record ManualBlockRenderState(
    Matrix3x2f pose, BlockState state, int x0, int y0, ScreenRectangle bounds
) implements PictureInPictureRenderState {
    public ManualBlockRenderState(Matrix3x2f pose, BlockState block, int x, int y) {
        this(pose, block, x, y, new ScreenRectangle(x, y, 27, 27).transformMaxBounds(pose));
    }

    @Override
    public int x1() {
        return x0 + 27;
    }

    @Override
    public int y1() {
        return y0 + 27;
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