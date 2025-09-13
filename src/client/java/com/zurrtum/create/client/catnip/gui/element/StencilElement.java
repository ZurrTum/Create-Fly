package com.zurrtum.create.client.catnip.gui.element;

import com.mojang.blaze3d.opengl.GlStateManager;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.opengl.GL11;

public interface StencilElement extends RenderElement {

    @Override
    default void render(DrawContext graphics) {
        graphics.getMatrices().pushMatrix();
        transform(graphics);
        prepareStencil(graphics);
        renderStencil(graphics);
        prepareElement(graphics);
        renderElement(graphics);
        cleanUp(graphics);
        graphics.getMatrices().popMatrix();
    }

    void renderStencil(DrawContext graphics);

    void renderElement(DrawContext graphics);

    default void transform(DrawContext graphics) {
        graphics.getMatrices().translate(getX(), getY());
    }

    default void prepareStencil(DrawContext graphics) {
        //        graphics.draw();
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        GL11.glStencilMask(~0);
        GlStateManager._clear(GL11.GL_STENCIL_BUFFER_BIT);
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_KEEP, GL11.GL_KEEP);
        GL11.glStencilMask(0xFF);
        GL11.glStencilFunc(GL11.GL_NEVER, 1, 0xFF);
    }

    default void prepareElement(DrawContext graphics) {
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
    }

    default void cleanUp(DrawContext graphics) {
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        //        graphics.draw();

    }
}
