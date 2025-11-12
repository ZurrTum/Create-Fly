package com.zurrtum.create.client.compat.rei.widget;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Function;

public class TooltipWidget extends Widget {
    private final Rectangle bounds;
    private final Function<Minecraft, Tooltip> tooltip;

    public TooltipWidget(int x, int y, int width, int height, Component... text) {
        this(x, y, width, height, mc -> Tooltip.create(text));
    }

    public TooltipWidget(int x, int y, int width, int height, List<Component> text) {
        this(x, y, width, height, mc -> Tooltip.create(text));
    }

    public TooltipWidget(int x, int y, int width, int height, Function<Minecraft, Tooltip> tooltip) {
        this.bounds = new Rectangle(x, y, width, height);
        this.tooltip = tooltip;
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return List.of();
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
        if (bounds.contains(mouseX, mouseY)) {
            Tooltip tooltip = this.tooltip.apply(minecraft);
            if (tooltip != null) {
                tooltip.queue();
            }
        }
    }
}
