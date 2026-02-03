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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.material.Fluid;

import java.util.ArrayList;
import java.util.List;

public record FluidStackRenderer(EntryRenderer<FluidStack> origin) implements EntryRenderer<FluidStack> {
    @Override
    public void render(EntryStack<FluidStack> entry, GuiGraphics graphics, Rectangle bounds, int mouseX, int mouseY, float delta) {
        FluidStack stack = entry.getValue();
        Fluid fluid = stack.getFluid();
        FluidConfig config = AllFluidConfigs.get(fluid);
        if (config == null) {
            return;
        }
        int color = config.tint().apply(stack.getComponents().asPatch()) | 0xff000000;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, config.still().get(), bounds.x, bounds.y, bounds.width, bounds.height, color);
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
                PatchedDataComponentMap components = stack.getComponents();
                PotionContents contents = components.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
                BottleType bottleType = components.getOrDefault(AllDataComponents.POTION_FLUID_BOTTLE_TYPE, BottleType.REGULAR);
                Component name = contents.getName(PotionFluidHandler.itemFromBottleType(bottleType).getDescriptionId() + ".effect.");
                List<Tooltip.Entry> list = new ArrayList<>();
                list.add(Tooltip.entry(name));
                Float scale = components.get(DataComponents.POTION_DURATION_SCALE);
                if (scale == null) {
                    if (bottleType == BottleType.LINGERING) {
                        scale = Items.LINGERING_POTION.components().getOrDefault(DataComponents.POTION_DURATION_SCALE, 1f);
                    } else {
                        scale = 1f;
                    }
                }
                PotionContents.addPotionTooltip(
                    contents.getAllEffects(),
                    text -> list.add(Tooltip.entry(text)),
                    scale,
                    context.vanillaContext().tickRate()
                );
                entries.removeFirst();
                entries.addAll(0, list);
            }
        }
        return tooltip;
    }
}
