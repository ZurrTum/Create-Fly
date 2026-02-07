package com.zurrtum.create.foundation.recipe;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.infrastructure.component.SequencedAssemblyJunk;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.IngredientPlacement;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.recipe.input.RecipeInput;
import org.jetbrains.annotations.Nullable;

public interface CreateRecipe<T extends RecipeInput> extends Recipe<T> {
    @Override
    default IngredientPlacement getIngredientPlacement() {
        return IngredientPlacement.NONE;
    }

    @Override
    default RecipeBookCategory getRecipeBookCategory() {
        return null;
    }

    @Override
    default boolean isIgnoredInRecipeBook() {
        return true;
    }

    @Nullable
    static ItemStack getJunk(ItemStack stack) {
        SequencedAssemblyJunk junk = stack.get(AllDataComponents.SEQUENCED_ASSEMBLY_JUNK);
        if (junk != null && junk.hasJunk()) {
            return junk.getJunk();
        }
        return null;
    }
}
