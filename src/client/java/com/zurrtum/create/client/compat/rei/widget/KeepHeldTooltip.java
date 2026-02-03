package com.zurrtum.create.client.compat.rei.widget;

import com.zurrtum.create.client.foundation.utility.CreateLang;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;

import java.util.function.BiFunction;

public class KeepHeldTooltip implements BiFunction<EntryStack<ItemStack>, Tooltip, Tooltip> {
    @Override
    public Tooltip apply(EntryStack<ItemStack> entryStack, Tooltip tooltip) {
        tooltip.entries().add(1, Tooltip.entry(CreateLang.translateDirect("recipe.deploying.not_consumed").withStyle(ChatFormatting.GOLD)));
        return tooltip;
    }
}
