package com.zurrtum.create.client.compat.eiv;

import de.crafty.eiv.common.api.recipe.IEivViewRecipe;
import net.minecraft.client.gui.screens.Screen;

public interface RecipeTransferHandler {
    boolean checkApplicable(Screen screen);

    boolean handle(Screen screen, IEivViewRecipe current, RecipeButton button, boolean craft);
}
