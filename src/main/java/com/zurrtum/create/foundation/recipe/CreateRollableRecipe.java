package com.zurrtum.create.foundation.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.random.Random;

import java.util.List;

public interface CreateRollableRecipe<T extends RecipeInput> extends CreateRecipe<T> {
    @Override
    default ItemStack craft(T input, RegistryWrapper.WrapperLookup registries) {
        return ItemStack.EMPTY;
    }

    List<ItemStack> craft(T input, Random random);
}
