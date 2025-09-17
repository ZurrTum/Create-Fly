package com.zurrtum.create.client.compat.rei;

import dev.architectury.fluid.FluidStack;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public interface IngredientHelper {
    static EntryIngredient getRenderEntryStack(EntryIngredient ingredient) {
        if (ingredient.getFirst().getValue() instanceof FluidStack) {
            for (EntryStack<FluidStack> stack : ingredient.<FluidStack>castAsList()) {
                stack.withRenderer(new FluidStackRenderer(stack.getRenderer()));
            }
        }
        return ingredient;
    }

    static List<EntryIngredient> condenseIngredients(List<EntryIngredient> ingredients) {
        List<ItemStack> cache = new ArrayList<>();
        List<EntryIngredient> result = new ArrayList<>();
        Find:
        for (EntryIngredient ingredient : ingredients) {
            if (ingredient.isEmpty()) {
                continue;
            }
            int size = ingredient.size();
            if (size != 1) {
                result.add(ingredient);
                continue;
            }
            EntryStack<?> entryStack = ingredient.getFirst();
            if (!(entryStack.getValue() instanceof ItemStack stack)) {
                result.add(ingredient);
                continue;
            }
            for (ItemStack target : cache) {
                if (ItemStack.areItemsAndComponentsEqual(stack, target)) {
                    target.increment(stack.getCount());
                    continue Find;
                }
            }
            stack = stack.copy();
            cache.add(stack);
            result.add(EntryIngredients.of(stack));
        }
        return result;
    }
}
