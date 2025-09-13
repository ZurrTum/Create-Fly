package com.zurrtum.create.content.kinetics.deployer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import com.zurrtum.create.AllRecipeSerializers;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.foundation.recipe.IngredientText;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.text.Text;

public record DeployerApplicationRecipe(
    ItemStack result, boolean keepHeldItem, Ingredient target, Ingredient ingredient
) implements ItemApplicationRecipe {
    @Override
    public RecipeSerializer<DeployerApplicationRecipe> getSerializer() {
        return AllRecipeSerializers.DEPLOYING;
    }

    @Override
    public RecipeType<DeployerApplicationRecipe> getType() {
        return AllRecipeTypes.DEPLOYING;
    }

    public static Text getDescriptionForAssembly(DynamicOps<JsonElement> ops, JsonObject object) {
        return Ingredient.CODEC.parse(ops, object.get("ingredient")).result()
            .map(ingredient -> Text.translatable("create.recipe.assembly.deploying_item", new IngredientText(ingredient)))
            .orElseGet(() -> Text.literal("Invalid"));
    }
}