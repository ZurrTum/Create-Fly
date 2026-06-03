package com.zurrtum.create.client.compat.rrv.category;

import cc.cassian.rrv.api.recipe.ReliableClientRecipe;
import cc.cassian.rrv.client.recipe.ClientRecipeManager;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.client.compat.rrv.CreateCategory;
import com.zurrtum.create.client.compat.rrv.view.CompactingView;
import com.zurrtum.create.content.kinetics.mixer.CompactingRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public class CompactingCategory extends CreateCategory {
    public static final CompactingCategory INSTANCE = new CompactingCategory();

    public static void register(ClientRecipeManager recipeManager, List<ReliableClientRecipe> output) {
        for (RecipeHolder<CompactingRecipe> entry : recipeManager.getRecipesForType(AllRecipeTypes.COMPACTING)) {
            output.add(new CompactingView(entry.id().identifier(), entry.value()));
        }
    }

    public CompactingCategory() {
        super("packing");
    }

    @Override
    public int getDisplayHeight() {
        return 99;
    }

    @Override
    public int getSlotCount() {
        return 17;
    }

    @Override
    public ItemStack getIcon() {
        return AllItems.MECHANICAL_PRESS.getDefaultInstance();
    }

    @Override
    public ItemStack getSubIcon() {
        return AllItems.BASIN.getDefaultInstance();
    }

    @Override
    public List<ItemStack> getCraftReferences() {
        return List.of(AllItems.MECHANICAL_PRESS.getDefaultInstance(), AllItems.BASIN.getDefaultInstance());
    }
}
