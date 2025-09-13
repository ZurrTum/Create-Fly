package com.zurrtum.create.client.catnip.gui.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import net.minecraft.client.render.item.KeyedItemRenderState;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public class ItemTransformRenderState implements SpecialGuiElementRenderState {
    public KeyedItemRenderState state;
    public boolean dirty;
    public Matrix3x2f pose;
    public ScreenRect bounds;
    public int x1, y1, x2, y2, padding;
    public float size, xRot, yRot, zRot;
    public @Nullable ScreenRect scissor;

    public void update(DrawContext graphics, ItemStack stack, float x, float y, float scale, int padding, float xRot, float yRot, float zRot) {
        Object modelKey = state == null ? null : state.getModelKey();
        state = new KeyedItemRenderState();
        state.displayContext = ItemDisplayContext.GUI;
        state.addModelKey(scale);
        state.addModelKey(xRot);
        state.addModelKey(yRot);
        state.addModelKey(zRot);
        MinecraftClient mc = graphics.client;
        mc.getItemModelManager().update(state, stack, state.displayContext, mc.world, mc.player, 0);
        if (modelKey != null && !state.getModelKey().equals(modelKey)) {
            dirty = true;
        }
        pose = new Matrix3x2f(graphics.getMatrices());
        size = scale * 16 + padding;
        x1 = (int) x;
        y1 = (int) y;
        x2 = (int) (x + size);
        y2 = (int) (y + size);
        this.padding = padding;
        bounds = new ScreenRect(x1, y1, (int) size, (int) size).transformEachVertex(pose);
        scissor = graphics.scissorStack.peekLast();
        if (scissor != null) {
            bounds = bounds.intersection(scissor);
        }
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

    @Override
    public @Nullable ScreenRect scissorArea() {
        return scissor;
    }
}
