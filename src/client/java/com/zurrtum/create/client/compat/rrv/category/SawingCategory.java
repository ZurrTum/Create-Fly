package com.zurrtum.create.client.compat.rrv.category;

import cc.cassian.rrv.api.recipe.ReliableClientRecipe;
import cc.cassian.rrv.client.recipe.ClientRecipeManager;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.client.compat.rrv.CreateCategory;
import com.zurrtum.create.client.compat.rrv.view.SawingView;
import com.zurrtum.create.content.kinetics.saw.CuttingRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public class SawingCategory extends CreateCategory {
    public static final SawingCategory INSTANCE = new SawingCategory();

    public static void register(ClientRecipeManager recipeManager, List<ReliableClientRecipe> output) {
        for (RecipeHolder<CuttingRecipe> entry : recipeManager.getRecipesForType(AllRecipeTypes.CUTTING)) {
            output.add(new SawingView(entry.id().identifier(), entry.value()));
        }
    }

    public SawingCategory() {
        super("sawing");
    }

    @Override
    public int getDisplayHeight() {
        return 62;
    }

    @Override
    public int getSlotCount() {
        return 5;
    }

    @Override
    public ItemStack getIcon() {
        return AllItems.MECHANICAL_SAW.getDefaultInstance();
    }

    @Override
    public ItemStack getSubIcon() {
        return Items.OAK_LOG.getDefaultInstance();
    }

    @Override
    public List<ItemStack> getCraftReferences() {
        return List.of(AllItems.MECHANICAL_SAW.getDefaultInstance());
    }
}

