package com.zurrtum.create.foundation.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.ArrayList;
import java.util.List;

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
    default boolean isSpecial() {
        return true;
    }

    default boolean isRollable() {
        return false;
    }

    default List<ItemStack> assemble(T input, RandomSource random) {
        List<ItemStack> list = new ArrayList<>(1);
        list.add(assemble(input, (HolderLookup.Provider) null));
        return list;
    }
}
