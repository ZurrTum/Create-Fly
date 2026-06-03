package com.zurrtum.create.client.compat.rrv.category;

import cc.cassian.rrv.api.recipe.ReliableClientRecipe;
import cc.cassian.rrv.client.recipe.ClientRecipeManager;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.client.compat.rrv.CreateCategory;
import com.zurrtum.create.client.compat.rrv.view.AutoCompactingView;
import com.zurrtum.create.content.kinetics.press.MechanicalPressBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;

import java.util.List;

public class AutoCompactingCategory extends CreateCategory {
    public static final AutoCompactingCategory INSTANCE = new AutoCompactingCategory();

    public static void register(ClientRecipeManager recipeManager, List<ReliableClientRecipe> output) {
        for (RecipeHolder<CraftingRecipe> entry : recipeManager.getRecipesForType(RecipeType.CRAFTING)) {
            CraftingRecipe recipe = entry.value();
            if (!MechanicalPressBlockEntity.canCompress(recipe) || AllRecipeTypes.shouldIgnoreInAutomation(entry)) {
                continue;
            }
            if (recipe instanceof ShapelessRecipe shapelessRecipe) {
                output.add(new AutoCompactingView(entry.id().identifier(), shapelessRecipe));
            } else if (recipe instanceof ShapedRecipe shapedRecipe) {
                output.add(new AutoCompactingView(entry.id().identifier(), shapedRecipe));
            }
        }
    }

    public AutoCompactingCategory() {
        super("automatic_packing");
    }

    @Override
    public int getDisplayHeight() {
        return 70;
    }

    @Override
    public int getSlotCount() {
        return 10;
    }

    @Override
    public ItemStack getIcon() {
        return AllItems.MECHANICAL_PRESS.getDefaultInstance();
    }

    @Override
    public ItemStack getSubIcon() {
        return Items.CRAFTING_TABLE.getDefaultInstance();
    }

    @Override
    public List<ItemStack> getCraftReferences() {
        return List.of(AllItems.MECHANICAL_PRESS.getDefaultInstance(), AllItems.BASIN.getDefaultInstance());
    }
}
