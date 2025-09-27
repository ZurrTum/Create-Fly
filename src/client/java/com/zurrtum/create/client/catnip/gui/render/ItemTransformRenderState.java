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

public record ItemTransformRenderState(
    KeyedItemRenderState state, Matrix3x2f pose, ScreenRect bounds, int x1, int y1, int x2, int y2, int padding, float size, float xRot, float yRot,
    float zRot, @Nullable ScreenRect scissor
) implements SpecialGuiElementRenderState {
    public static ItemTransformRenderState create(
        DrawContext graphics,
        ItemStack stack,
        float x,
        float y,
        float scale,
        int padding,
        float xRot,
        float yRot,
        float zRot
    ) {
        KeyedItemRenderState state = new KeyedItemRenderState();
        state.displayContext = ItemDisplayContext.GUI;
        state.addModelKey(scale);
        state.addModelKey(padding);
        state.addModelKey(xRot);
        state.addModelKey(yRot);
        state.addModelKey(zRot);
        MinecraftClient mc = graphics.client;
        mc.getItemModelManager().update(state, stack, state.displayContext, mc.world, mc.player, 0);
        Matrix3x2f pose = new Matrix3x2f(graphics.getMatrices());
        float size = scale * 16 + padding;
        int x1 = (int) x;
        int y1 = (int) y;
        int x2 = (int) (x + size);
        int y2 = (int) (y + size);
        ScreenRect bounds = new ScreenRect(x1, y1, (int) size, (int) size).transformEachVertex(pose);
        ScreenRect scissor = graphics.scissorStack.peekLast();
        if (scissor != null) {
            bounds = bounds.intersection(scissor);
        }
        return new ItemTransformRenderState(state, pose, bounds, x1, y1, x2, y2, padding, size, xRot, yRot, zRot, scissor);
    }

    public Object getKey() {
        return state.getModelKey();
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
