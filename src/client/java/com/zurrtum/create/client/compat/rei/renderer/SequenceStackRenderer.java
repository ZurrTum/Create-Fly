package com.zurrtum.create.client.compat.rei.renderer;

import dev.architectury.fluid.FluidStack;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public record SequenceStackRenderer<T>(List<Text> tooltip, EntryRenderer<T> origin) implements EntryRenderer<T> {
    public static void setRenderer(EntryIngredient ingredient, List<Text> tooltip) {
        if (ingredient.getFirst().getValue() instanceof FluidStack) {
            for (EntryStack<FluidStack> stack : ingredient.<FluidStack>castAsList()) {
                stack.withRenderer(new FluidStackRenderer(stack.getRenderer()));
            }
        }
        for (EntryStack<?> stack : ingredient) {
            setRenderer(stack, tooltip);
        }
    }

    private static <T> void setRenderer(EntryStack<T> stack, List<Text> tooltip) {
        stack.withRenderer(new SequenceStackRenderer<>(new ArrayList<>(tooltip), stack.getRenderer()));
    }

    @Override
    public void render(EntryStack<T> entry, DrawContext graphics, Rectangle bounds, int mouseX, int mouseY, float delta) {
        origin.render(entry, graphics, bounds, mouseX, mouseY, delta);
    }

    @Override
    public Tooltip getTooltip(EntryStack<T> entry, TooltipContext context) {
        Tooltip tooltip = origin.getTooltip(entry, context);
        if (tooltip == null) {
            return Tooltip.create(this.tooltip);
        }
        List<Tooltip.Entry> entries = tooltip.entries();
        for (int i = this.tooltip.size() - 1; i >= 0; i--) {
            entries.addFirst(Tooltip.entry(this.tooltip.get(i)));
        }
        return tooltip;
    }
}
