package com.zurrtum.create.foundation.recipe.trie;

import net.minecraft.recipe.Recipe;

import java.util.Set;

public class AbstractRecipe<R extends Recipe<?>> {
    final R recipe;
    final Set<AbstractIngredient> ingredients;

    public AbstractRecipe(R recipe, Set<AbstractIngredient> ingredients) {
        this.recipe = recipe;
        this.ingredients = ingredients;
    }
}
