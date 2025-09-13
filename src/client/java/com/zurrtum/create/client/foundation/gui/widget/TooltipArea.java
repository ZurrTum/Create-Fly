package com.zurrtum.create.client.foundation.gui.widget;

import com.zurrtum.create.client.catnip.gui.widget.AbstractSimiWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.List;

public class TooltipArea extends AbstractSimiWidget {

    public TooltipArea(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    public void renderWidget(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        if (visible)
            hovered = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + width && mouseY < getY() + height;
    }

    public TooltipArea withTooltip(List<Text> tooltip) {
        this.toolTip = tooltip;
        return this;
    }

}
