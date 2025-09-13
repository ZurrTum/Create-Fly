package com.zurrtum.create.client.foundation.gui.widget;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.client.catnip.gui.widget.AbstractSimiWidget;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class Indicator extends AbstractSimiWidget {

    public State state;

    public Indicator(int x, int y, Text tooltip) {
        super(x, y, AllGuiTextures.INDICATOR.getWidth(), AllGuiTextures.INDICATOR.getHeight());
        this.toolTip = toolTip.isEmpty() ? ImmutableList.of() : ImmutableList.of(tooltip);
        this.state = State.OFF;
    }

    @Override
    public void render(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        if (!visible)
            return;
        AllGuiTextures toDraw = switch (state) {
            case ON -> AllGuiTextures.INDICATOR_WHITE;
            case OFF -> AllGuiTextures.INDICATOR;
            case RED -> AllGuiTextures.INDICATOR_RED;
            case YELLOW -> AllGuiTextures.INDICATOR_YELLOW;
            case GREEN -> AllGuiTextures.INDICATOR_GREEN;
        };
        toDraw.render(graphics, getX(), getY());
    }

    public enum State {
        OFF,
        ON,
        RED,
        YELLOW,
        GREEN;
    }

}
