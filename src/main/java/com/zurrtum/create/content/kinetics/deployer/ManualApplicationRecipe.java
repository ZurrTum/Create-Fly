package com.zurrtum.create.content.kinetics.deployer;

import com.zurrtum.create.AllRecipeSerializers;
import com.zurrtum.create.AllRecipeTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

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
