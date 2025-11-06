package com.zurrtum.create.client.compat.jei.renderer;

import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public record IconRenderer(ItemStack icon) implements IDrawable {
    public IconRenderer(Item icon) {
        this(new ItemStack(icon));
    }

    @Override
    public int getWidth() {
        return 16;
    }

    @Override
    public int getHeight() {
        return 16;
    }

    @Override
    public void draw(DrawContext graphics, int x, int y) {
        graphics.drawItem(icon, x, y);
    }
}
