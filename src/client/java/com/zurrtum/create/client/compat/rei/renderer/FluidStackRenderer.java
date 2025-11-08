package com.zurrtum.create.client.compat.rei.renderer;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllFluids;
import com.zurrtum.create.client.AllFluidConfigs;
import com.zurrtum.create.client.infrastructure.fluid.FluidConfig;
import com.zurrtum.create.content.fluids.potion.PotionFluidHandler;
import com.zurrtum.create.infrastructure.component.BottleType;
import dev.architectury.fluid.FluidStack;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.MergedComponentMap;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;

public record FluidStackRenderer(EntryRenderer<FluidStack> origin) implements EntryRenderer<FluidStack> {
    @Override
    public void render(EntryStack<FluidStack> entry, DrawContext graphics, Rectangle bounds, int mouseX, int mouseY, float delta) {
        FluidStack stack = entry.getValue();
        Fluid fluid = stack.getFluid();
        FluidConfig config = AllFluidConfigs.get(fluid);
        if (config == null) {
            return;
        }
        int color = config.tint().apply(stack.getComponents().getChanges()) | 0xff000000;
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
            FluidStack stack = entry.getValue();
            if (stack.getFluid() == AllFluids.POTION) {
                MergedComponentMap components = stack.getComponents();
                PotionContentsComponent contents = components.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
                BottleType bottleType = components.getOrDefault(AllDataComponents.POTION_FLUID_BOTTLE_TYPE, BottleType.REGULAR);
                Text name = contents.getName(PotionFluidHandler.itemFromBottleType(bottleType).getTranslationKey() + ".effect.");
                List<Tooltip.Entry> list = new ArrayList<>();
                list.add(Tooltip.entry(name));
                contents.appendTooltip(context.vanillaContext(), text -> list.add(Tooltip.entry(text)), context.getFlag(), components);
                entries.removeFirst();
                entries.addAll(0, list);
            }
        }
        return tooltip;
    }
}
