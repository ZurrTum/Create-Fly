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

public class BlockTransformRenderState implements SpecialGuiElementRenderState {
    public BlockState state;
    public List<BlockModelPart> parts;
    public boolean dirty;
    public Matrix3x2f pose;
    public ScreenRect bounds;
    public int x1, y1, x2, y2, padding;
    public float size, xRot, yRot, zRot;
    public @Nullable ScreenRect scissor;

    public void update(DrawContext graphics, BlockState block, float x, float y, float scale, int padding, float xRot, float yRot, float zRot) {
        float size = scale * 16 + padding;
        if (state != block) {
            dirty = state != null;
            state = block;
            MinecraftClient mc = graphics.client;
            parts = mc.getBlockRenderManager().getModel(state).getParts(mc.world.random);
        } else if (size != this.size || xRot != this.xRot || yRot != this.yRot || zRot != this.zRot) {
            dirty = true;
        }
        pose = new Matrix3x2f(graphics.getMatrices());
        x1 = (int) x;
        y1 = (int) y;
        x2 = (int) (x + size);
        y2 = (int) (y + size);
        bounds = new ScreenRect(x1, y1, (int) size, (int) size).transformEachVertex(pose);
        scissor = graphics.scissorStack.peekLast();
        if (scissor != null) {
            bounds = bounds.intersection(scissor);
        }
        this.size = size;
        this.padding = padding;
        this.xRot = xRot;
        this.yRot = yRot;
        this.zRot = zRot;
    }

    public void clearDirty() {
        dirty = false;
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
