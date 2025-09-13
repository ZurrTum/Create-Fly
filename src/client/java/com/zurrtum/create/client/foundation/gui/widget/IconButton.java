package com.zurrtum.create.client.foundation.gui.widget;

import com.zurrtum.create.client.AllKeys;
import com.zurrtum.create.client.catnip.gui.element.ScreenElement;
import com.zurrtum.create.client.catnip.gui.widget.AbstractSimiWidget;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class IconButton extends AbstractSimiWidget {

    protected ScreenElement icon;

    public boolean green;

    public IconButton(int x, int y, ScreenElement icon) {
        this(x, y, 18, 18, icon);
    }

    public IconButton(int x, int y, int w, int h, ScreenElement icon) {
        super(x, y, w, h);
        this.icon = icon;
    }

    @Override
    public void doRender(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        if (visible) {
            hovered = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + width && mouseY < getY() + height;

            AllGuiTextures button = !active ? AllGuiTextures.BUTTON_DISABLED : hovered && AllKeys.isMouseButtonDown(0) ? AllGuiTextures.BUTTON_DOWN : hovered ? AllGuiTextures.BUTTON_HOVER : green ? AllGuiTextures.BUTTON_GREEN : AllGuiTextures.BUTTON;

            drawBg(graphics, button);
            icon.render(graphics, getX() + 1, getY() + 1);
        }
    }

    protected void drawBg(DrawContext graphics, AllGuiTextures button) {
        graphics.drawTexture(
            RenderPipelines.GUI_TEXTURED,
            button.location,
            getX(),
            getY(),
            button.getStartX(),
            button.getStartY(),
            button.getWidth(),
            button.getHeight(),
            256,
            256
        );
    }

    public void setToolTip(Text text) {
        toolTip.clear();
        toolTip.add(text);
    }

    public void setIcon(ScreenElement icon) {
        this.icon = icon;
    }
}
