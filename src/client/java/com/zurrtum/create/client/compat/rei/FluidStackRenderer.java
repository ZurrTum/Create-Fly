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
import org.jetbrains.annotations.Nullable;

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
    public @Nullable Tooltip getTooltip(EntryStack<FluidStack> entry, TooltipContext context) {
        return origin.getTooltip(entry, context);
    }
}
