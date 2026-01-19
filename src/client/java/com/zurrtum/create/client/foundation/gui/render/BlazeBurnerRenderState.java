package com.zurrtum.create.client.foundation.gui.render;

import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

public record BlazeBurnerRenderState(
    Matrix3x2f pose, int x, int y, Level world, BlockState block, BlazeBurnerBlock.HeatLevel heatLevel, float animation, boolean drawGoggles,
    int hash, ScreenRectangle bounds
) implements PictureInPictureRenderState {
    public BlazeBurnerRenderState(
        Matrix3x2f pose,
        int x,
        int y,
        Level world,
        BlockState block,
        BlazeBurnerBlock.HeatLevel heatLevel,
        float animation,
        boolean drawGoggles,
        int hash
    ) {
        this(pose, x, y, world, block, heatLevel, animation, drawGoggles, hash, new ScreenRectangle(x, y, 68, 68).transformMaxBounds(pose));
    }

    @Override
    public @Nullable ScreenRectangle bounds() {
        return bounds;
    }

    @Override
    public Matrix3x2f pose() {
        return IDENTITY_POSE;
    }

    @Override
    public float scale() {
        return 48;
    }

    @Override
    public @Nullable ScreenRectangle scissorArea() {
        return null;
    }

    @Override
    public int x0() {
        return x;
    }

    @Override
    public int x1() {
        return x + 68;
    }

    @Override
    public int y0() {
        return y;
    }

    @Override
    public int y1() {
        return y + 68;
    }
}
