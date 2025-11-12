package com.zurrtum.create.client.compat.jei.renderer;

import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.gui.GuiGraphics;

public class SlotRenderer implements IDrawable {
    private final AllGuiTextures texture;
    private final int width;
    private final int height;

    public SlotRenderer(AllGuiTextures texture, int width, int height) {
        this.texture = texture;
        this.width = width;
        this.height = height;
    }

    public SlotRenderer(AllGuiTextures texture) {
        this(texture, texture.getWidth(), texture.getHeight());
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void draw(GuiGraphics graphics, int x, int y) {
        if (texture != null) {
            texture.render(graphics, x, y);
        }
    }
}
