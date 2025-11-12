package com.zurrtum.create.client.catnip.render;

import com.zurrtum.create.catnip.theme.Color;
import net.minecraft.client.gui.GuiGraphics;

public interface ColoredRenderable {

    void render(GuiGraphics graphics, int x, int y, Color c);

}
