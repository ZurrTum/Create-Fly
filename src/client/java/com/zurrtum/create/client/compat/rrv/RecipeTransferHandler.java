package com.zurrtum.create.client.compat.rrv;

import cc.cassian.rrv.api.recipe.ReliableClientRecipe;
import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import net.minecraft.client.gui.screens.Screen;

public interface RecipeTransferHandler {
    boolean checkApplicable(Screen screen, ReliableClientRecipeType type);

    boolean handle(Screen screen, ReliableClientRecipe current, RecipeButton button, boolean craft);
}