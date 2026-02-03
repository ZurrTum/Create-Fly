package com.zurrtum.create.client.compat.jei.widget;

import com.mojang.datafixers.util.Either;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class KeepHeldTooltip implements IRecipeSlotRichTooltipCallback {
    private static final Either<FormattedText, TooltipComponent> KEEP_HELD = Either.left(CreateLang.translateDirect("recipe.deploying.not_consumed")
        .withStyle(ChatFormatting.GOLD));

    @Override
    public void onRichTooltip(IRecipeSlotView iRecipeSlotView, ITooltipBuilder tooltip) {
        tooltip.getLines().add(1, KEEP_HELD);
    }
}
