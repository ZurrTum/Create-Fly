package com.zurrtum.create.client.foundation.gui.render;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record SandPaperRenderState(Matrix3x2f pose, ItemStack stack, int x0, int y0, ScreenRectangle bounds) implements PictureInPictureRenderState {
    public SandPaperRenderState(Matrix3x2f pose, ItemStack stack, int x, int y) {
        this(pose, stack, x, y, new ScreenRectangle(x, y, 28, 29).transformMaxBounds(pose));
    }

    @Override
    public int x1() {
        return x0 + 28;
    }

    @Override
    public int y1() {
        return y0 + 29;
    }

    @Override
    public float scale() {
        return 32;
    }

    @Override
    public @Nullable ScreenRectangle scissorArea() {
        return null;
    }
}