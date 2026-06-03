package com.zurrtum.create.client.compat.rrv.category;

import cc.cassian.rrv.api.recipe.ReliableClientRecipe;
import cc.cassian.rrv.client.recipe.ClientRecipeManager;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.client.compat.rrv.CreateCategory;
import com.zurrtum.create.client.compat.rrv.view.CrushingView;
import com.zurrtum.create.content.kinetics.crusher.CrushingRecipe;
import com.zurrtum.create.content.kinetics.millstone.MillingRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public class CrushingCategory extends CreateCategory {
    public static final CrushingCategory INSTANCE = new CrushingCategory();

    public static void register(ClientRecipeManager recipeManager, List<ReliableClientRecipe> output) {
        for (RecipeHolder<CrushingRecipe> entry : recipeManager.getRecipesForType(AllRecipeTypes.CRUSHING)) {
            output.add(new CrushingView(entry.id().identifier(), entry.value()));
        }
        for (RecipeHolder<MillingRecipe> entry : recipeManager.getRecipesForType(AllRecipeTypes.MILLING)) {
            output.add(new CrushingView(entry.id().identifier(), entry.value()));
        }
    }

    public CrushingCategory() {
        super("crushing");
    }

    @Override
    public int getDisplayHeight() {
        return 98;
    }

    @Override
    public int getSlotCount() {
        return 8;
    }

    @Override
    public ItemStack getIcon() {
        return AllItems.CRUSHING_WHEEL.getDefaultInstance();
    }

    @Override
    public ItemStack getSubIcon() {
        return AllItems.CRUSHED_GOLD.getDefaultInstance();
    }

    @Override
    public List<ItemStack> getCraftReferences() {
        return List.of(AllItems.CRUSHING_WHEEL.getDefaultInstance());
    }
}
