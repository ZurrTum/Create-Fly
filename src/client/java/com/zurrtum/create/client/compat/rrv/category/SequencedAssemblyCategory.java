package com.zurrtum.create.client.compat.rrv.category;

import cc.cassian.rrv.api.recipe.ReliableClientRecipe;
import cc.cassian.rrv.client.recipe.ClientRecipeManager;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.client.compat.rrv.CreateCategory;
import com.zurrtum.create.client.compat.rrv.view.SequencedAssemblyView;
import com.zurrtum.create.content.processing.sequenced.SequencedAssemblyRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public class SequencedAssemblyCategory extends CreateCategory {
    public static final SequencedAssemblyCategory INSTANCE = new SequencedAssemblyCategory();

    public static void register(ClientRecipeManager recipeManager, List<ReliableClientRecipe> output) {
        for (RecipeHolder<SequencedAssemblyRecipe> entry : recipeManager.getRecipesForType(AllRecipeTypes.SEQUENCED_ASSEMBLY)) {
            output.add(new SequencedAssemblyView(entry.id().identifier(), entry.value()));
        }
    }

    public SequencedAssemblyCategory() {
        super("sequenced_assembly");
    }

    @Override
    public int getDisplayHeight() {
        return 119;
    }

    @Override
    public int getSlotCount() {
        return 10;
    }

    @Override
    public ItemStack getIcon() {
        return AllItems.PRECISION_MECHANISM.getDefaultInstance();
    }
}
