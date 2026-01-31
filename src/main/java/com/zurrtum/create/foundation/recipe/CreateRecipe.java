package com.zurrtum.create.foundation.recipe;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.infrastructure.component.SequencedAssemblyJunk;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jspecify.annotations.Nullable;

public interface CreateRecipe<T extends RecipeInput> extends Recipe<T> {
    @Override
    default PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    default RecipeBookCategory recipeBookCategory() {
        return null;
    }

    @Override
    default String group() {
        return "";
    }

    @Override
    default boolean showNotification() {
        return true;
    }

    @Override
    default boolean isSpecial() {
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
