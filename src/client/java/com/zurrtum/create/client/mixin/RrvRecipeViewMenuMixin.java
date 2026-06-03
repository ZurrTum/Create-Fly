package com.zurrtum.create.client.mixin;

import cc.cassian.rrv.api.recipe.ReliableClientRecipe;
import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.compat.rrv.CreateView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RecipeViewMenu.class)
public class RrvRecipeViewMenuMixin {
    @WrapOperation(method = "updateByPage()V", at = @At(value = "INVOKE", target = "Lcc/cassian/rrv/api/recipe/ReliableClientRecipeType;placeSlots(Lcc/cassian/rrv/common/recipe/inventory/RecipeViewMenu$SlotDefinition;)V"), remap = false)
    private void placeSlots(
        ReliableClientRecipeType type,
        RecipeViewMenu.SlotDefinition slotDefinition,
        Operation<Void> original,
        @Local(name = "recipe") ReliableClientRecipe recipe
    ) {
        if (recipe instanceof CreateView view) {
            view.placeSlots(slotDefinition);
        } else {
            original.call(type, slotDefinition);
        }
    }
}
