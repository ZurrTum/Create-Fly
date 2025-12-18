package com.zurrtum.create.content.kinetics.crusher;

import com.zurrtum.create.content.processing.recipe.ChanceOutput;
import com.zurrtum.create.foundation.recipe.CreateSingleStackRollableRecipe;
import net.minecraft.recipe.Ingredient;

import java.util.List;

public interface AbstractCrushingRecipe extends CreateSingleStackRollableRecipe {
    int time();

    List<ChanceOutput> results();

    Ingredient ingredient();
}
