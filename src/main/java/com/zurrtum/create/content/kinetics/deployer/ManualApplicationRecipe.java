package com.zurrtum.create.content.kinetics.deployer;

import com.zurrtum.create.AllRecipeSerializers;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;

public record ManualApplicationRecipe(
    List<ProcessingOutput> results, boolean keepHeldItem, Ingredient target, Ingredient ingredient
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
