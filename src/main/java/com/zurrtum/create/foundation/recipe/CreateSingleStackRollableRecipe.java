package com.zurrtum.create.foundation.recipe;

import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public interface CreateSingleStackRollableRecipe extends CreateRollableRecipe<SingleRecipeInput> {
    Ingredient ingredient();

    @Override
    default boolean matches(SingleRecipeInput input, Level world) {
        return ingredient().test(input.item());
    }

    List<ProcessingOutput> results();

    @Override
    default List<ItemStack> assemble(SingleRecipeInput input, RandomSource random) {
        ItemStack junk = CreateRecipe.getJunk(input.item());
        if (junk != null) {
            return List.of(junk);
        }
        List<ProcessingOutput> results = results();
        List<ItemStack> outputs = new ArrayList<>(results.size());
        ProcessingOutput.rollOutput(random, results, outputs::add);
        return outputs;
    }
}
