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
    public float size, xRadians, yRadians, zRadians;

    public ItemTransformRenderKey(ItemStack stack) {
        this.stack = stack;
        Minecraft mc = Minecraft.getInstance();
        state.displayContext = ItemDisplayContext.GUI;
        mc.getItemModelResolver().appendItemLayers(state, stack, state.displayContext, mc.level, mc.player, 0);
    }

    public void update(float scale, int padding, float xRadians, float yRadians, float zRadians) {
        float size = scale * 16 + padding;
        if (size != this.size || xRadians != this.xRadians || yRadians != this.yRadians || zRadians != this.zRadians) {
            dirty = true;
            this.size = size;
            this.padding = padding;
            this.xRadians = xRadians;
            this.yRadians = yRadians;
            this.zRadians = zRadians;
        }
    }
}
