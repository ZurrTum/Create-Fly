package com.zurrtum.create.client.compat.rrv.category;

import cc.cassian.rrv.api.recipe.ReliableClientRecipe;
import cc.cassian.rrv.client.recipe.ClientRecipeManager;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.client.compat.rrv.CreateCategory;
import com.zurrtum.create.client.compat.rrv.view.MechanicalCraftingView;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCraftingRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public class MechanicalCraftingCategory extends CreateCategory {
    public static final MechanicalCraftingCategory INSTANCE = new MechanicalCraftingCategory();

    public static void register(ClientRecipeManager recipeManager, List<ReliableClientRecipe> output) {
        for (RecipeHolder<MechanicalCraftingRecipe> entry : recipeManager.getRecipesForType(AllRecipeTypes.MECHANICAL_CRAFTING)) {
            output.add(new MechanicalCraftingView(entry.id().identifier(), entry.value()));
        }
    }

    public MechanicalCraftingCategory() {
        super("mechanical_crafting");
    }

    @Override
    public int getDisplayHeight() {
        return 94;
    }

    @Override
    public int getSlotCount() {
        return 26;
    }

    @Override
    public ItemStack getIcon() {
        return AllItems.MECHANICAL_CRAFTER.getDefaultInstance();
    }

    @Override
    public List<ItemStack> getCraftReferences() {
        return List.of(AllItems.MECHANICAL_CRAFTER.getDefaultInstance());
    }
}
