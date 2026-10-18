package com.zurrtum.create.client.catnip.gui.element;

import com.mojang.renderpearl.backend.opengl.GlStateManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.lwjgl.opengl.GL11;

public interface StencilElement extends RenderElement {

    @Override
    default void render(GuiGraphicsExtractor graphics) {
        graphics.pose().pushMatrix();
        transform(graphics);
        prepareStencil(graphics);
        renderStencil(graphics);
        prepareElement(graphics);
        renderElement(graphics);
        cleanUp(graphics);
        graphics.pose().popMatrix();
    }

    void renderStencil(GuiGraphicsExtractor graphics);

    void renderElement(GuiGraphicsExtractor graphics);

    default void transform(GuiGraphicsExtractor graphics) {
        graphics.pose().translate(getX(), getY());
    }

    default void prepareStencil(GuiGraphicsExtractor graphics) {
        //        graphics.draw();
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        GL11.glStencilMask(~0);
        GlStateManager._clear(GL11.GL_STENCIL_BUFFER_BIT);
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_KEEP, GL11.GL_KEEP);
        GL11.glStencilMask(0xFF);
        GL11.glStencilFunc(GL11.GL_NEVER, 1, 0xFF);
    }

    default void prepareElement(GuiGraphicsExtractor graphics) {
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
    }

    default void cleanUp(GuiGraphicsExtractor graphics) {
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        //        graphics.draw();

    }
}
