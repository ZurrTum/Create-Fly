package com.zurrtum.create.compat.fabric;

import com.zurrtum.create.AllRecipeSerializers;
import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;

public class RecipeCommonPlugin {
    public static void register() {
        RecipeSynchronization.synchronizeRecipeSerializer(AllRecipeSerializers.COMPACTING);
        RecipeSynchronization.synchronizeRecipeSerializer(AllRecipeSerializers.PRESSING);
        RecipeSynchronization.synchronizeRecipeSerializer(AllRecipeSerializers.MIXING);
        RecipeSynchronization.synchronizeRecipeSerializer(AllRecipeSerializers.MILLING);
        RecipeSynchronization.synchronizeRecipeSerializer(AllRecipeSerializers.CUTTING);
        RecipeSynchronization.synchronizeRecipeSerializer(AllRecipeSerializers.CRUSHING);
        RecipeSynchronization.synchronizeRecipeSerializer(AllRecipeSerializers.ITEM_APPLICATION);
        RecipeSynchronization.synchronizeRecipeSerializer(AllRecipeSerializers.DEPLOYING);
        RecipeSynchronization.synchronizeRecipeSerializer(AllRecipeSerializers.SANDPAPER_POLISHING);
        RecipeSynchronization.synchronizeRecipeSerializer(AllRecipeSerializers.EMPTYING);
        RecipeSynchronization.synchronizeRecipeSerializer(AllRecipeSerializers.MECHANICAL_CRAFTING);
        RecipeSynchronization.synchronizeRecipeSerializer(AllRecipeSerializers.FILLING);
        RecipeSynchronization.synchronizeRecipeSerializer(AllRecipeSerializers.SEQUENCED_ASSEMBLY);
        RecipeSynchronization.synchronizeRecipeSerializer(AllRecipeSerializers.HAUNTING);
        RecipeSynchronization.synchronizeRecipeSerializer(AllRecipeSerializers.SPLASHING);
        RecipeSynchronization.synchronizeRecipeSerializer(AllRecipeSerializers.POTION);
    }
}
