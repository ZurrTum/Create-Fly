package com.zurrtum.create.client.compat.rei.renderer;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.compat.GuiGraphics;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;

public record TwoIconRenderer(ItemStack icon, ItemStack subIcon) implements Renderer {
    public TwoIconRenderer(Item icon, Item subIcon) {
        this(new ItemStack(icon), new ItemStack(subIcon));
    }

    @Override
    public void render(GuiGraphics graphics, Rectangle bounds, int mouseX, int mouseY, float delta) {
        Matrix3x2fStack matrices = graphics.pose();
        matrices.pushMatrix();
        matrices.translate(bounds.x, bounds.y);
        matrices.scale(bounds.getWidth() / 16.0f, bounds.getHeight() / 16.0f);
        graphics.item(icon, 0, 0);
        matrices.translate(9, 9);
        matrices.scale(0.5f, 0.5f);
        graphics.item(subIcon, 0, 0);
        matrices.popMatrix();
    }
}
