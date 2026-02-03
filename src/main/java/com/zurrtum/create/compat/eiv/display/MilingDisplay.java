package com.zurrtum.create.compat.eiv.display;

import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import com.zurrtum.create.foundation.recipe.CreateSingleStackRollableRecipe;
import de.crafty.eiv.common.api.recipe.EivRecipeType;
import de.crafty.eiv.common.api.recipe.IEivServerRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public class MilingDisplay extends CrushingDisplay {
    public MilingDisplay() {
    }

    public MilingDisplay(RecipeHolder<? extends CreateSingleStackRollableRecipe> entry) {
        super(entry);
    }

    @Override
    public EivRecipeType<? extends IEivServerRecipe> getRecipeType() {
        return EivCommonPlugin.MILLING;
    }
}
