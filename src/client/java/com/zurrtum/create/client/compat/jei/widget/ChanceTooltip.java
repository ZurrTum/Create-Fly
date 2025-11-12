package com.zurrtum.create.client.compat.jei.widget;

import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.processing.recipe.ChanceOutput;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import net.minecraft.ChatFormatting;

public record ChanceTooltip(float chance) implements IRecipeSlotRichTooltipCallback {
    public ChanceTooltip(ChanceOutput output) {
        this(output.chance());
    }

    @Override
    public void onRichTooltip(IRecipeSlotView recipeSlotView, ITooltipBuilder tooltip) {
        tooltip.add(CreateLang.translateDirect("recipe.processing.chance", chance < 0.01 ? "<1" : (int) (chance * 100)).withStyle(ChatFormatting.GOLD));
    }
}
