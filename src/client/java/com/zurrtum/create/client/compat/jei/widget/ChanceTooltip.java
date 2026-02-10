package com.zurrtum.create.client.compat.jei.widget;

import com.mojang.datafixers.util.Either;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record ChanceTooltip(Either<FormattedText, TooltipComponent> chance) implements IRecipeSlotRichTooltipCallback {
    public ChanceTooltip(float chance) {
        this(Either.left(CreateLang.translateDirect("recipe.processing.chance", chance < 0.01 ? "<1" : (int) (chance * 100)).withStyle(ChatFormatting.GOLD)));
    }

    @Override
    public void onRichTooltip(IRecipeSlotView recipeSlotView, ITooltipBuilder tooltip) {
        tooltip.getLines().add(1, chance);
    }
}
