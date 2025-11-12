package com.zurrtum.create.client.compat.jei.widget;

import com.mojang.datafixers.util.Either;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllFluids;
import com.zurrtum.create.content.fluids.potion.PotionFluidHandler;
import com.zurrtum.create.infrastructure.component.BottleType;
import mezz.jei.api.fabric.constants.FabricTypes;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;

public class PotionTooltip implements IRecipeSlotRichTooltipCallback {
    @Override
    public void onRichTooltip(IRecipeSlotView recipeSlotView, ITooltipBuilder tooltip) {
        List<Either<FormattedText, TooltipComponent>> lines = tooltip.getLines();
        if (!lines.isEmpty()) {
            lines.removeFirst();
        }
        recipeSlotView.getDisplayedIngredient(FabricTypes.FLUID_STACK).ifPresent(ingredient -> {
            FluidVariant variant = ingredient.getFluidVariant();
            if (variant.isOf(AllFluids.POTION)) {
                DataComponentMap components = variant.getComponentMap();
                PotionContents contents = components.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
                BottleType bottleType = components.getOrDefault(AllDataComponents.POTION_FLUID_BOTTLE_TYPE, BottleType.REGULAR);
                ItemLike itemFromBottleType = PotionFluidHandler.itemFromBottleType(bottleType);
                Component name = contents.getName(itemFromBottleType.asItem().getDescriptionId() + ".effect.");
                List<Either<FormattedText, TooltipComponent>> list = new ArrayList<>();
                list.add(Either.left(name));
                contents.addToTooltip(Item.TooltipContext.EMPTY, text -> list.add(Either.left(text)), TooltipFlag.NORMAL, components);
                lines.addAll(0, list);
            }
        });
    }
}
