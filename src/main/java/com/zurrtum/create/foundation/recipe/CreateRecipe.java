package com.zurrtum.create.foundation.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.IngredientPlacement;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.random.Random;

import java.util.List;

public interface CreateRecipe<T extends RecipeInput> extends Recipe<T> {
    @Override
    default IngredientPlacement getIngredientPlacement() {
        return IngredientPlacement.NONE;
    }

    @Override
    default RecipeBookCategory getRecipeBookCategory() {
        return null;
    }

    @Override
    default boolean isIgnoredInRecipeBook() {
        return true;
    }

    default boolean isRollable() {
        return false;
    }

    default List<ItemStack> craft(T input, Random random) {
        return List.of(craft(input, (RegistryWrapper.WrapperLookup) null));
    }
}
