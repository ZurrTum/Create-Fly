package com.zurrtum.create.client.foundation.gui.render;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record FanRenderState(Matrix3x2f pose, int x0, int y0, BlockState target, ScreenRectangle bounds) implements PictureInPictureRenderState {
    public FanRenderState(Matrix3x2f pose, int x, int y, BlockState target) {
        this(pose, x, y, target, new ScreenRectangle(x, y, 50, 44).transformMaxBounds(pose));
    }

    @Override
    public int x1() {
        return x0 + 50;
    }

    @Override
    public int y1() {
        return y0 + 44;
    }

    @Override
    public float scale() {
        return 24;
    }

    @Override
    public @Nullable ScreenRectangle scissorArea() {
        return null;
    }
}
