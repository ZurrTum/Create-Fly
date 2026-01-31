package com.zurrtum.create.foundation.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

public interface CreateSingleStackRecipe extends CreateRecipe<SingleRecipeInput> {
    ItemStackTemplate result();

    Ingredient ingredient();

    @Override
    default boolean matches(SingleRecipeInput input, Level world) {
        return ingredient().test(input.item());
    }

    @Override
    default ItemStack assemble(SingleRecipeInput input) {
        ItemStack junk = CreateRecipe.getJunk(input.item());
        if (junk != null) {
            return junk;
        }
        return result().create();
    }
}
