package com.zurrtum.create.client.compat.eiv;

import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu;
import net.minecraft.client.gui.screens.Screen;

public record RecipeTransferContext(RecipeViewMenu menu, Screen screen, RecipeTransferHandler handler) {
}
