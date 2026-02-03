package com.zurrtum.create.content.kinetics.deployer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import com.zurrtum.create.AllRecipeSerializers;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.foundation.recipe.IngredientText;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;

public record DeployerApplicationRecipe(
    List<ProcessingOutput> results, boolean keepHeldItem, Ingredient target, Ingredient ingredient
) implements ItemApplicationRecipe {
    @Override
    public RecipeSerializer<DeployerApplicationRecipe> getSerializer() {
        return AllRecipeSerializers.DEPLOYING;
    }

    @Override
    public RecipeType<DeployerApplicationRecipe> getType() {
        return AllRecipeTypes.DEPLOYING;
    }

    public static Component getDescriptionForAssembly(DynamicOps<JsonElement> ops, JsonObject object) {
        return Ingredient.CODEC.parse(ops, object.get("ingredient")).result()
            .map(ingredient -> Component.translatable("create.recipe.assembly.deploying_item", new IngredientText(ingredient)))
            .orElseGet(() -> Component.literal("Invalid"));
    }
}