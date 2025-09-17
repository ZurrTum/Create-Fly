package com.zurrtum.create.client.compat.rei;

import com.zurrtum.create.client.AllFluidConfigs;
import com.zurrtum.create.client.infrastructure.fluid.FluidConfig;
import dev.architectury.fluid.FluidStack;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.util.List;

public record FluidStackRenderer(EntryRenderer<FluidStack> origin) implements EntryRenderer<FluidStack> {
    @Override
    public void render(EntryStack<FluidStack> entry, DrawContext graphics, Rectangle bounds, int mouseX, int mouseY, float delta) {
        Fluid fluid = entry.getValue().getFluid();
        FluidConfig config = AllFluidConfigs.get(fluid);
        if (config == null) {
            return;
        }
        int color = config.tint().get() | 0xff000000;
        graphics.drawSpriteStretched(RenderPipelines.GUI_TEXTURED, config.still().get(), bounds.x, bounds.y, bounds.width, bounds.height, color);
    }

    @Override
    public Tooltip getTooltip(EntryStack<FluidStack> entry, TooltipContext context) {
        Tooltip tooltip = origin.getTooltip(entry, context);
        if (tooltip == null) {
            return null;
        }
        List<Tooltip.Entry> entries = tooltip.entries();
        Tooltip.Entry first = entries.getFirst();
        if (first.isText()) {
            Text name = first.getAsText();
            if (name.getString().startsWith("block.")) {
                entries.removeFirst();
                entries.addFirst(Tooltip.entry(Text.translatable(Util.createTranslationKey(
                    "fluid",
                    Registries.FLUID.getId(entry.getValue().getFluid())
                ))));
            }
        }
        return tooltip;
    }
}
