package com.zurrtum.create.foundation.recipe;

import com.zurrtum.create.content.processing.recipe.ChanceOutput;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.List;

public interface CreateRollableRecipe<T extends RecipeInput> extends CreateRecipe<T> {
    @Override
    default boolean isRollable() {
        return true;
    }

    @Override
    default ItemStack craft(T input, RegistryWrapper.WrapperLookup registries) {
        return ItemStack.EMPTY;
    }

    List<ChanceOutput> results();

    @Override
    default List<ItemStack> craft(T input, Random random) {
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
