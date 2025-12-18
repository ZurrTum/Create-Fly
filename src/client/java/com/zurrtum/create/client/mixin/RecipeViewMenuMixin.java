package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.compat.eiv.CreateView;
import de.crafty.eiv.common.api.recipe.IEivRecipeViewType;
import de.crafty.eiv.common.api.recipe.IEivViewRecipe;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RecipeViewMenu.class)
public class RecipeViewMenuMixin {
    @WrapOperation(method = "updateByPage()V", at = @At(value = "INVOKE", target = "Lde/crafty/eiv/common/api/recipe/IEivRecipeViewType;placeSlots(Lde/crafty/eiv/common/recipe/inventory/RecipeViewMenu$SlotDefinition;)V"), remap = false)
    private void placeSlots(
        IEivRecipeViewType type,
        RecipeViewMenu.SlotDefinition slotDefinition,
        Operation<Void> original,
        @Local(name = "recipe") IEivViewRecipe recipe
    ) {
        if (recipe instanceof CreateView view) {
            view.placeSlots(slotDefinition);
        } else {
            original.call(type, slotDefinition);
        }
    }
}
