package com.zurrtum.create.client.compat.jei.widget;

import com.zurrtum.create.client.foundation.utility.CreateLang;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public record JunkChanceTooltip(Component chance) implements IRecipeSlotRichTooltipCallback {
    private static final Component JUNK = CreateLang.translateDirect("recipe.assembly.junk");

    public JunkChanceTooltip(float chance) {
        this(CreateLang.translateDirect("recipe.processing.chance", chance < 0.01 ? "<1" : chance > 0.99 ? ">99" : Math.round(chance * 100))
            .withStyle(ChatFormatting.GOLD));
    }

    @Override
    public void onRichTooltip(IRecipeSlotView iRecipeSlotView, ITooltipBuilder tooltip) {
        tooltip.add(JUNK);
        tooltip.add(chance);
    }
}
