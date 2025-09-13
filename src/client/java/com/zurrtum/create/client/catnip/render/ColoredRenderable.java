package com.zurrtum.create.client.catnip.render;

import com.zurrtum.create.catnip.theme.Color;
import net.minecraft.client.gui.DrawContext;

public interface ColoredRenderable {

    void render(DrawContext graphics, int x, int y, Color c);

}
