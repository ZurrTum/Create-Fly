package com.zurrtum.create.content.kinetics.deployer;

import com.zurrtum.create.AllRecipeSerializers;
import com.zurrtum.create.AllRecipeTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;

public record ManualApplicationRecipe(
    ItemStack result, boolean keepHeldItem, Ingredient target, Ingredient ingredient
) implements ItemApplicationRecipe {
    @Override
    public RecipeSerializer<ManualApplicationRecipe> getSerializer() {
        return AllRecipeSerializers.ITEM_APPLICATION;
    }

    @Override
    public RecipeType<ManualApplicationRecipe> getType() {
        return AllRecipeTypes.ITEM_APPLICATION;
    }
}
