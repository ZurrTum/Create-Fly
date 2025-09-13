package com.zurrtum.create.client.catnip.gui.element;

import net.minecraft.client.gui.DrawContext;

@FunctionalInterface
public interface FadableScreenElement extends ScreenElement {

    @Override
    default void render(DrawContext graphics, int x, int y) {
        render(graphics, x, y, 1f);
    }

    void render(DrawContext graphics, int x, int y, float alpha);

}
