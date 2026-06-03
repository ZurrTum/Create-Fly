package com.zurrtum.create.client.compat.rrv.category;

import cc.cassian.rrv.api.recipe.ReliableClientRecipe;
import cc.cassian.rrv.client.recipe.ClientRecipeManager;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.client.compat.rrv.CreateCategory;
import com.zurrtum.create.client.compat.rrv.view.AutoMixingView;
import com.zurrtum.create.content.kinetics.press.MechanicalPressBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import java.util.List;

public class AutoMixingCategory extends CreateCategory {
    public static final AutoMixingCategory INSTANCE = new AutoMixingCategory();

    public static void register(ClientRecipeManager recipeManager, List<ReliableClientRecipe> output) {
        for (RecipeHolder<CraftingRecipe> entry : recipeManager.getRecipesForType(RecipeType.CRAFTING)) {
            CraftingRecipe recipe = entry.value();
            if (!(recipe instanceof ShapelessRecipe shapelessRecipe) || MechanicalPressBlockEntity.canCompress(
                shapelessRecipe) || AllRecipeTypes.shouldIgnoreInAutomation(entry) || shapelessRecipe.ingredients.size() == 1) {
                continue;
            }
            output.add(new AutoMixingView(entry.id().identifier(), shapelessRecipe));
        }
    }

    public AutoMixingCategory() {
        super("automatic_shapeless");
    }

    @Override
    public int getDisplayHeight() {
        return 70;
    }

    @Override
    public int getSlotCount() {
        return 9;
    }

    @Override
    public ItemStack getIcon() {
        return AllItems.MECHANICAL_MIXER.getDefaultInstance();
    }

    @Override
    public ItemStack getSubIcon() {
        return Items.CRAFTING_TABLE.getDefaultInstance();
    }

    @Override
    public List<ItemStack> getCraftReferences() {
        return List.of(AllItems.MECHANICAL_MIXER.getDefaultInstance(), AllItems.BASIN.getDefaultInstance());
    }
}
