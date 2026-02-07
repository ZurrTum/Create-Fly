package com.zurrtum.create.client.compat.jei.widget;

import com.mojang.datafixers.util.Either;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.text.StringVisitable;
import net.minecraft.util.Formatting;

public class KeepHeldTooltip implements IRecipeSlotRichTooltipCallback {
    private static final Either<StringVisitable, TooltipData> KEEP_HELD = Either.left(CreateLang.translateDirect("recipe.deploying.not_consumed")
        .formatted(Formatting.GOLD));

    @Override
    public void onRichTooltip(IRecipeSlotView iRecipeSlotView, ITooltipBuilder tooltip) {
        tooltip.getLines().add(1, KEEP_HELD);
    }
}
