package com.zurrtum.create.client.compat.rrv.category;

import cc.cassian.rrv.api.recipe.ReliableClientRecipe;
import cc.cassian.rrv.client.recipe.ClientRecipeManager;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.client.compat.rrv.CreateCategory;
import com.zurrtum.create.client.compat.rrv.view.SandPaperPolishingView;
import com.zurrtum.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public class SandPaperPolishingCategory extends CreateCategory {
    public static final SandPaperPolishingCategory INSTANCE = new SandPaperPolishingCategory();

    public static void register(ClientRecipeManager recipeManager, List<ReliableClientRecipe> output) {
        for (RecipeHolder<SandPaperPolishingRecipe> entry : recipeManager.getRecipesForType(AllRecipeTypes.SANDPAPER_POLISHING)) {
            output.add(new SandPaperPolishingView(entry.id().identifier(), entry.value()));
        }
    }

    public SandPaperPolishingCategory() {
        super("sandpaper_polishing");
    }

    @Override
    public int getDisplayHeight() {
        return 48;
    }

    @Override
    public int getSlotCount() {
        return 2;
    }

    @Override
    public ItemStack getIcon() {
        return AllItems.SAND_PAPER.getDefaultInstance();
    }

    @Override
    public List<ItemStack> getCraftReferences() {
        return List.of(AllItems.SAND_PAPER.getDefaultInstance(), AllItems.RED_SAND_PAPER.getDefaultInstance());
    }
}
