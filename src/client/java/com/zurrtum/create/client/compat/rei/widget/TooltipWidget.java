package com.zurrtum.create.client.compat.rei.widget;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Function;

public class TooltipWidget extends Widget {
    private final Rectangle bounds;
    private final Function<MinecraftClient, Tooltip> tooltip;

    public TooltipWidget(int x, int y, int width, int height, Text... text) {
        this(x, y, width, height, mc -> Tooltip.create(text));
    }

    public TooltipWidget(int x, int y, int width, int height, List<Text> text) {
        this(x, y, width, height, mc -> Tooltip.create(text));
    }

    public TooltipWidget(int x, int y, int width, int height, Function<MinecraftClient, Tooltip> tooltip) {
        this.bounds = new Rectangle(x, y, width, height);
        this.tooltip = tooltip;
    }

    @Override
    public List<? extends Element> children() {
        return List.of();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        if (bounds.contains(mouseX, mouseY)) {
            Tooltip tooltip = this.tooltip.apply(minecraft);
            if (tooltip != null) {
                tooltip.queue();
            }
        }
    }
}
