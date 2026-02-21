package com.zurrtum.create.client.catnip.gui.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ItemTransformRenderKey {
    public ItemStack stack;
    public TrackingItemStackRenderState state = new TrackingItemStackRenderState();
    public boolean dirty;
    public int padding;
    public float size, xRot, yRot, zRot;

    public ItemTransformRenderKey(ItemStack stack) {
        this.stack = stack;
        Minecraft mc = Minecraft.getInstance();
        state.displayContext = ItemDisplayContext.GUI;
        mc.getItemModelResolver().appendItemLayers(state, stack, state.displayContext, mc.level, mc.player, 0);
    }

    public void update(float scale, int padding, float xRot, float yRot, float zRot) {
        float size = scale * 16 + padding;
        if (size != this.size || xRot != this.xRot || yRot != this.yRot || zRot != this.zRot) {
            dirty = true;
            this.size = size;
            this.padding = padding;
            this.xRot = xRot;
            this.yRot = yRot;
            this.zRot = zRot;
        }
    }
}
