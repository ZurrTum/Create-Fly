package com.zurrtum.create.client.compat.rei;

import dev.architectury.fluid.FluidStack;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;

import java.util.Collection;

public interface IngredientHelper {
    static Collection<? extends EntryStack<?>> getRenderEntryStack(EntryIngredient ingredient) {
        if (ingredient.getFirst().getValue() instanceof FluidStack) {
            for (EntryStack<FluidStack> stack : ingredient.<FluidStack>castAsList()) {
                stack.withRenderer(new FluidStackRenderer(stack.getRenderer()));
            }
            return ingredient;
        } else {
            return ingredient;
        }
    }
}
