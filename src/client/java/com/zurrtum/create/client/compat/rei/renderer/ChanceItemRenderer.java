package com.zurrtum.create.client.compat.rei.renderer;

import com.zurrtum.create.client.foundation.utility.CreateLang;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record ChanceItemRenderer(float chance, EntryRenderer<ItemStack> origin) implements EntryRenderer<ItemStack> {
    @Override
    public void render(EntryStack<ItemStack> entry, GuiGraphics graphics, Rectangle bounds, int mouseX, int mouseY, float delta) {
        origin.render(entry, graphics, bounds, mouseX, mouseY, delta);
    }

    @Override
    public @Nullable Tooltip getTooltip(EntryStack<ItemStack> entry, TooltipContext context) {
        Tooltip tooltip = origin.getTooltip(entry, context);
        if (tooltip != null) {
            tooltip.entries().add(
                1,
                Tooltip.entry(CreateLang.translateDirect("recipe.processing.chance", chance < 0.01 ? "<1" : (int) (chance * 100))
                    .withStyle(ChatFormatting.GOLD))
            );
        }
        return tooltip;
    }
}
