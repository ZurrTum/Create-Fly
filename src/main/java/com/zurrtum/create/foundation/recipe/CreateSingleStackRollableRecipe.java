package com.zurrtum.create.foundation.recipe;

import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public interface CreateSingleStackRollableRecipe extends CreateRollableRecipe<SingleStackRecipeInput> {
    Ingredient ingredient();

    @Override
    default boolean matches(SingleStackRecipeInput input, World world) {
        return ingredient().test(input.item());
    }

    List<ProcessingOutput> results();

    @Override
    default List<ItemStack> craft(SingleStackRecipeInput input, Random random) {
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
