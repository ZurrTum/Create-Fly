package com.zurrtum.create.client.compat.jei.renderer;

import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;

public record TwoIconRenderer(ItemStack icon, ItemStack subIcon) implements IDrawable {
    public TwoIconRenderer(Item icon, Item subIcon) {
        this(new ItemStack(icon), new ItemStack(subIcon));
    }

    @Override
    public int getWidth() {
        return 18;
    }

    @Override
    public int getHeight() {
        return 18;
    }

    @Override
    public void draw(GuiGraphics graphics, int x, int y) {
        Matrix3x2fStack matrices = graphics.pose();
        matrices.pushMatrix();
        matrices.translate(x, y);
        graphics.renderItem(icon, 1, 1);
        matrices.translate(9, 9);
        matrices.scale(0.5f, 0.5f);
        graphics.renderItem(subIcon, 2, 2);
        matrices.popMatrix();
    }
}
