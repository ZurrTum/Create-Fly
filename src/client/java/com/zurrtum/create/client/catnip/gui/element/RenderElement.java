package com.zurrtum.create.client.catnip.gui.element;

import net.minecraft.client.gui.DrawContext;

public interface RenderElement extends FadableScreenElement {

    static RenderElement of(ScreenElement renderable) {
        return new AbstractRenderElement.SimpleRenderElement(renderable);
    }

    <T extends RenderElement> T at(float x, float y);

    <T extends RenderElement> T at(float x, float y, float z);

    <T extends RenderElement> T withBounds(int width, int height);

    <T extends RenderElement> T withAlpha(float alpha);

    int getWidth();

    int getHeight();

    float getX();

    float getY();

    float getZ();

    void render(DrawContext graphics);

    @Override
    default void render(DrawContext graphics, int x, int y, float alpha) {
        this.at(x, y).withAlpha(alpha).render(graphics);
    }

    default void clear() {
    }
}
