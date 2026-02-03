package com.zurrtum.create.client.compat.jei.widget;

import com.zurrtum.create.client.foundation.utility.CreateLang;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public record ChanceTooltip(Component chance) implements IRecipeSlotRichTooltipCallback {
    public ChanceTooltip(float chance) {
        this(CreateLang.translateDirect("recipe.processing.chance", chance < 0.01 ? "<1" : (int) (chance * 100)).withStyle(ChatFormatting.GOLD));
    }

    @Override
    public void onRichTooltip(IRecipeSlotView recipeSlotView, ITooltipBuilder tooltip) {
        tooltip.add(chance);
    }
}
