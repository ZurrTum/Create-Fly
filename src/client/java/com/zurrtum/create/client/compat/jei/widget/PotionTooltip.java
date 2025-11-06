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
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class PotionTooltip implements IRecipeSlotRichTooltipCallback {
    @Override
    public void onRichTooltip(IRecipeSlotView recipeSlotView, ITooltipBuilder tooltip) {
        List<Either<StringVisitable, TooltipData>> lines = tooltip.getLines();
        if (!lines.isEmpty()) {
            lines.removeFirst();
        }
        recipeSlotView.getDisplayedIngredient(FabricTypes.FLUID_STACK).ifPresent(ingredient -> {
            FluidVariant variant = ingredient.getFluidVariant();
            if (variant.isOf(AllFluids.POTION)) {
                ComponentMap components = variant.getComponentMap();
                PotionContentsComponent contents = components.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
                BottleType bottleType = components.getOrDefault(AllDataComponents.POTION_FLUID_BOTTLE_TYPE, BottleType.REGULAR);
                ItemConvertible itemFromBottleType = PotionFluidHandler.itemFromBottleType(bottleType);
                Text name = contents.getName(itemFromBottleType.asItem().getTranslationKey() + ".effect.");
                List<Either<StringVisitable, TooltipData>> list = new ArrayList<>();
                list.add(Either.left(name));
                contents.appendTooltip(Item.TooltipContext.DEFAULT, text -> list.add(Either.left(text)), TooltipType.BASIC, components);
                lines.addAll(0, list);
            }
        });
    }
}
