package com.zurrtum.create.foundation.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.List;

public interface CreateRollableRecipe<T extends RecipeInput> extends CreateRecipe<T> {
    @Override
    default ItemStack assemble(T input, HolderLookup.Provider provider) {
        return ItemStack.EMPTY;
    }

    List<ItemStack> assemble(T input, RandomSource random);
}
