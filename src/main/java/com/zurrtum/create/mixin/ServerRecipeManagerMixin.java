package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.AllRecipeSets;
import com.zurrtum.create.content.kinetics.mixer.PotionRecipe;
import com.zurrtum.create.content.processing.sequenced.SequencedAssemblyRecipe;
import net.minecraft.recipe.PreparedRecipes;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipePropertySet;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.recipe.ServerRecipeManager.SoleIngredientGetter;
import net.minecraft.registry.RegistryKey;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Stream;

@Mixin(ServerRecipeManager.class)
public class ServerRecipeManagerMixin {
    @Inject(method = "prepare(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)Lnet/minecraft/recipe/PreparedRecipes;", at = @At(value = "INVOKE", target = "Ljava/util/SortedMap;size()I"))
    private void addSequencedAssemblyRecipe(
        ResourceManager resourceManager,
        Profiler profiler,
        CallbackInfoReturnable<PreparedRecipes> cir,
        @Local SortedMap<Identifier, Recipe<?>> sortedMap
    ) {
        sortedMap.putAll(SequencedAssemblyRecipe.Serializer.GENERATE_RECIPES);
        PotionRecipe.register(sortedMap);
    }

    @WrapOperation(method = "initialize(Lnet/minecraft/resource/featuretoggle/FeatureSet;)V", at = @At(value = "INVOKE", target = "Ljava/util/Set;stream()Ljava/util/stream/Stream;"))
    public Stream<Entry<RegistryKey<RecipePropertySet>, SoleIngredientGetter>> registerRecipeSet(
        Set<Entry<RegistryKey<RecipePropertySet>, SoleIngredientGetter>> instance,
        Operation<Stream<Entry<RegistryKey<RecipePropertySet>, SoleIngredientGetter>>> original
    ) {
        return Stream.concat(original.call(instance), AllRecipeSets.ALL.entrySet().stream());
    }
}
