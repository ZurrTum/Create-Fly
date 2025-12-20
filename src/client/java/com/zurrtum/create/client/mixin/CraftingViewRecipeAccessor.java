package com.zurrtum.create.client.mixin;

import de.crafty.eiv.common.builtin.shaped.CraftingViewRecipe;
import de.crafty.eiv.common.recipe.inventory.SlotContent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.HashMap;

@Mixin(CraftingViewRecipe.class)
public interface CraftingViewRecipeAccessor {
    @Accessor(value = "ingredientSlotContents", remap = false)
    HashMap<Integer, SlotContent> getIngredientSlotContents();
}
