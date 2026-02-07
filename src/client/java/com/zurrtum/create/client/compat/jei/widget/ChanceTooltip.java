package com.zurrtum.create.client.compat.jei.widget;

import com.mojang.datafixers.util.Either;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.text.StringVisitable;
import net.minecraft.util.Formatting;

public record ChanceTooltip(Either<StringVisitable, TooltipData> chance) implements IRecipeSlotRichTooltipCallback {
    public ChanceTooltip(float chance) {
        this(Either.left(CreateLang.translateDirect("recipe.processing.chance", chance < 0.01 ? "<1" : (int) (chance * 100)).formatted(Formatting.GOLD)));
    }

    @Override
    public void onRichTooltip(IRecipeSlotView recipeSlotView, ITooltipBuilder tooltip) {
        tooltip.getLines().add(1, chance);
    }
}
