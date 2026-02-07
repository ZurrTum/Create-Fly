package com.zurrtum.create.compat.eiv.display;

import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import com.zurrtum.create.foundation.recipe.CreateSingleStackRollableRecipe;
import de.crafty.eiv.common.api.recipe.EivRecipeType;
import de.crafty.eiv.common.api.recipe.IEivServerRecipe;
import net.minecraft.recipe.RecipeEntry;

public class MilingDisplay extends CrushingDisplay {
    public MilingDisplay() {
    }

    public MilingDisplay(RecipeEntry<? extends CreateSingleStackRollableRecipe> entry) {
        super(entry);
    }

    @Override
    public EivRecipeType<? extends IEivServerRecipe> getRecipeType() {
        return EivCommonPlugin.MILLING;
    }
}
