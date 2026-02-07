package com.zurrtum.create.foundation.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

public interface CreateSingleStackRecipe extends CreateRecipe<SingleStackRecipeInput> {
    ItemStack result();

    Ingredient ingredient();

    @Override
    default boolean matches(SingleStackRecipeInput input, World world) {
        return ingredient().test(input.item());
    }

    @Override
    default ItemStack craft(SingleStackRecipeInput input, RegistryWrapper.WrapperLookup registries) {
        ItemStack junk = CreateRecipe.getJunk(input.item());
        if (junk != null) {
            return junk;
        }
        return result().copy();
    }
}
