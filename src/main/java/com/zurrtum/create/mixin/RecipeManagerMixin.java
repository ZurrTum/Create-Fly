package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.AllRecipeSets;
import com.zurrtum.create.content.kinetics.mixer.PotionRecipe;
import com.zurrtum.create.content.processing.sequenced.SequencedAssemblyRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Stream;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeManager.IngredientExtractor;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipePropertySet;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {
    @Inject(method = "prepare(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)Lnet/minecraft/world/item/crafting/RecipeMap;", at = @At(value = "INVOKE", target = "Ljava/util/SortedMap;size()I"))
    private void addSequencedAssemblyRecipe(
        ResourceManager resourceManager,
        ProfilerFiller profiler,
        CallbackInfoReturnable<RecipeMap> cir,
        @Local SortedMap<Identifier, Recipe<?>> sortedMap
    ) {
        sortedMap.putAll(SequencedAssemblyRecipe.Serializer.GENERATE_RECIPES);
        PotionRecipe.register(sortedMap);
    }

    @WrapOperation(method = "finalizeRecipeLoading(Lnet/minecraft/world/flag/FeatureFlagSet;)V", at = @At(value = "INVOKE", target = "Ljava/util/Set;stream()Ljava/util/stream/Stream;"))
    public Stream<Entry<ResourceKey<RecipePropertySet>, IngredientExtractor>> registerRecipeSet(
        Set<Entry<ResourceKey<RecipePropertySet>, IngredientExtractor>> instance,
        Operation<Stream<Entry<ResourceKey<RecipePropertySet>, IngredientExtractor>>> original
    ) {
        return Stream.concat(original.call(instance), AllRecipeSets.ALL.entrySet().stream());
    }
}
