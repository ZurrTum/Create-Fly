package com.zurrtum.create.client.content.trains.station;

import com.zurrtum.create.client.catnip.gui.element.ScreenElement;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;

public class WideIconButton extends IconButton {

    public WideIconButton(int x, int y, ScreenElement icon) {
        super(x, y, 26, 18, icon);
    }

    @Override
    protected void drawBg(GuiGraphics graphics, AllGuiTextures button) {
        super.drawBg(graphics, button);
        graphics.blit(
            RenderPipelines.GUI_TEXTURED,
            button.location,
            getX() + 9,
            getY(),
            button.getStartX() + 1,
            button.getStartY(),
            button.getWidth() - 1,
            button.getHeight(),
            256,
            256
        );
    }

}
