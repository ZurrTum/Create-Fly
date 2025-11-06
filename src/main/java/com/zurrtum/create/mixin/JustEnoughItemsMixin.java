package com.zurrtum.create.mixin;

import com.zurrtum.create.AllRecipeSerializers;
import mezz.jei.fabric.JustEnoughItems;
import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JustEnoughItems.class)
public class JustEnoughItemsMixin {
    @Inject(method = "onInitialize()V", at = @At("TAIL"))
    private void syncRecipe(CallbackInfo ci) {
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
