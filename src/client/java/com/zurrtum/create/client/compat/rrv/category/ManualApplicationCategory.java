package com.zurrtum.create.client.compat.rrv.category;

import cc.cassian.rrv.api.recipe.ReliableClientRecipe;
import cc.cassian.rrv.client.recipe.ClientRecipeManager;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.client.compat.rrv.CreateCategory;
import com.zurrtum.create.client.compat.rrv.view.ManualApplicationView;
import com.zurrtum.create.content.kinetics.deployer.ManualApplicationRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public class ManualApplicationCategory extends CreateCategory {
    public static final ManualApplicationCategory INSTANCE = new ManualApplicationCategory();

    public static void register(ClientRecipeManager recipeManager, List<ReliableClientRecipe> output) {
        for (RecipeHolder<ManualApplicationRecipe> entry : recipeManager.getRecipesForType(AllRecipeTypes.ITEM_APPLICATION)) {
            output.add(new ManualApplicationView(entry.id().identifier(), entry.value()));
        }
    }

    public ManualApplicationCategory() {
        super("item_application");
    }

    @Override
    public int getDisplayHeight() {
        return 59;
    }

    @Override
    public int getSlotCount() {
        return 6;
    }

    @Override
    public ItemStack getIcon() {
        return AllItems.BRASS_HAND.getDefaultInstance();
    }
}
