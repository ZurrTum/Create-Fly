package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.AllRecipeSets;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeManager.IngredientExtractor;
import net.minecraft.world.item.crafting.RecipePropertySet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {
    @WrapOperation(method = "finalizeRecipeLoading(Lnet/minecraft/world/flag/FeatureFlagSet;)V", at = @At(value = "INVOKE", target = "Ljava/util/Set;stream()Ljava/util/stream/Stream;"))
    public Stream<Entry<ResourceKey<RecipePropertySet>, IngredientExtractor>> registerRecipeSet(
        Set<Entry<ResourceKey<RecipePropertySet>, IngredientExtractor>> instance,
        Operation<Stream<Entry<ResourceKey<RecipePropertySet>, IngredientExtractor>>> original
    ) {
        return Stream.concat(original.call(instance), AllRecipeSets.ALL.entrySet().stream());
    }
}
