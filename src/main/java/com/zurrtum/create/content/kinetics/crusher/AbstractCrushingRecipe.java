package com.zurrtum.create.content.kinetics.crusher;

import com.zurrtum.create.content.processing.recipe.ChanceOutput;
import com.zurrtum.create.foundation.recipe.CreateSingleStackRollableRecipe;
import java.util.List;
import net.minecraft.world.item.crafting.Ingredient;

public interface AbstractCrushingRecipe extends CreateSingleStackRollableRecipe {
    int time();

    List<ChanceOutput> results();

    Ingredient ingredient();
}
