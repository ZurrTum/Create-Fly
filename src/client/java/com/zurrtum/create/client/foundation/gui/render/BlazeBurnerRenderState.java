package com.zurrtum.create.client.foundation.gui.render;

import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record BlazeBurnerRenderState(
    Matrix3x2f pose, int x, int y, World world, BlockState block, BlazeBurnerBlock.HeatLevel heatLevel, float animation,
    boolean drawGoggles,
    int hash, ScreenRect bounds
) implements SpecialGuiElementRenderState {
    public BlazeBurnerRenderState(
        Matrix3x2f pose,
        int x,
        int y,
        World world,
        BlockState block,
        BlazeBurnerBlock.HeatLevel heatLevel,
        float animation,
        boolean drawGoggles,
        int hash
    ) {
        this(pose, x, y, world, block, heatLevel, animation, drawGoggles, hash, new ScreenRect(x, y, 68, 68).transformEachVertex(pose));
    }

    @Override
    public float scale() {
        return 48;
    }

    @Override
    public @Nullable ScreenRect scissorArea() {
        return null;
    }

    @Override
    public int x1() {
        return x;
    }

    @Override
    public int x2() {
        return x + 68;
    }

    @Override
    public int y1() {
        return y;
    }

    @Override
    public int y2() {
        return y + 68;
    }
}
