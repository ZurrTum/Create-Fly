package com.zurrtum.create.client.catnip.gui.render;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import net.minecraft.client.render.model.BlockModelPart;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

import java.util.List;

public record BlockTransformRenderState(
    BlockState state, List<BlockModelPart> parts, Matrix3x2f pose, ScreenRect bounds, int x1, int y1, int x2, int y2, int padding, float size,
    float xRot, float yRot, float zRot, @Nullable ScreenRect scissor
) implements SpecialGuiElementRenderState {
    public static BlockTransformRenderState create(
        DrawContext graphics,
        BlockState block,
        float x,
        float y,
        float scale,
        int padding,
        float xRot,
        float yRot,
        float zRot
    ) {
        float size = scale * 16 + padding;
        MinecraftClient mc = graphics.client;
        List<BlockModelPart> parts = mc.getBlockRenderManager().getModel(block).getParts(mc.world.random);
        Matrix3x2f pose = new Matrix3x2f(graphics.getMatrices());
        int x1 = (int) x;
        int y1 = (int) y;
        int x2 = (int) (x + size);
        int y2 = (int) (y + size);
        ScreenRect bounds = new ScreenRect(x1, y1, (int) size, (int) size).transformEachVertex(pose);
        ScreenRect scissor = graphics.scissorStack.peekLast();
        if (scissor != null) {
            bounds = bounds.intersection(scissor);
        }
        return new BlockTransformRenderState(block, parts, pose, bounds, x1, y1, x2, y2, padding, size, xRot, yRot, zRot, scissor);
    }

    public Object getKey() {
        return List.of(state, size, padding, xRot, yRot, zRot);
    }

    public static Object getKey(BlockState state, float scale, int padding, float xRot, float yRot, float zRot) {
        return List.of(state, scale * 16 + padding, padding, xRot, yRot, zRot);
    }

    @Override
    public int x1() {
        return x1;
    }

    @Override
    public int x2() {
        return x2;
    }

    @Override
    public int y1() {
        return y1;
    }

    @Override
    public int y2() {
        return y2;
    }

    @Override
    public Matrix3x2f pose() {
        return pose;
    }

    @Override
    public @Nullable ScreenRect bounds() {
        return bounds;
    }

    @Override
    public float scale() {
        return size;
    }

    public @Nullable ScreenRect scissorArea() {
        return scissor;
    }
}
