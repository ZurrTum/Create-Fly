package com.zurrtum.create.client.catnip.gui.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record ItemTransformRenderState(
    TrackingItemStackRenderState state, Matrix3x2f pose, ScreenRectangle bounds, int x0, int y0, int x1, int y1, int padding, float scale, float xRot,
    float yRot, float zRot, @Nullable ScreenRectangle scissorArea
) implements PictureInPictureRenderState {
    public static ItemTransformRenderState create(
        GuiGraphics graphics,
        ItemStack stack,
        float x,
        float y,
        float scale,
        int padding,
        float xRot,
        float yRot,
        float zRot
    ) {
        TrackingItemStackRenderState state = new TrackingItemStackRenderState();
        state.displayContext = ItemDisplayContext.GUI;
        state.appendModelIdentityElement(scale);
        state.appendModelIdentityElement(padding);
        state.appendModelIdentityElement(xRot);
        state.appendModelIdentityElement(yRot);
        state.appendModelIdentityElement(zRot);
        Minecraft mc = graphics.minecraft;
        mc.getItemModelResolver().appendItemLayers(state, stack, state.displayContext, mc.level, mc.player, 0);
        Matrix3x2f pose = new Matrix3x2f(graphics.pose());
        float size = scale * 16 + padding;
        int x1 = (int) x;
        int y1 = (int) y;
        int x2 = (int) (x + size);
        int y2 = (int) (y + size);
        ScreenRectangle bounds = new ScreenRectangle(x1, y1, (int) size, (int) size).transformMaxBounds(pose);
        ScreenRectangle scissor = graphics.scissorStack.peek();
        if (scissor != null) {
            bounds = bounds.intersection(scissor);
        }
        return new ItemTransformRenderState(state, pose, bounds, x1, y1, x2, y2, padding, size, xRot, yRot, zRot, scissor);
    }

    public Object getKey() {
        return state.getModelIdentity();
    }
}
