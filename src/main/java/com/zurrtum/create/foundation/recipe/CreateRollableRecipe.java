package com.zurrtum.create.foundation.recipe;

import com.zurrtum.create.content.processing.recipe.ChanceOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.ArrayList;
import java.util.List;

public interface CreateRollableRecipe<T extends RecipeInput> extends CreateRecipe<T> {
    @Override
    default boolean isRollable() {
        return true;
    }

    @Override
    default ItemStack assemble(T input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    List<ChanceOutput> results();

    @Override
    default List<ItemStack> assemble(T input, RandomSource random) {
        List<ItemStack> list = new ArrayList<>();
        for (ChanceOutput output : results()) {
            ItemStack stack = output.get(random);
            if (stack != null) {
                list.add(stack);
            }
        }
        return list;
    }
}
