package com.zurrtum.create.client.compat.rrv.category;

import cc.cassian.rrv.api.recipe.ReliableClientRecipe;
import cc.cassian.rrv.client.recipe.ClientRecipeManager;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.client.compat.rrv.CreateCategory;
import com.zurrtum.create.client.compat.rrv.view.DeployingView;
import com.zurrtum.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.zurrtum.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.zurrtum.create.content.kinetics.deployer.ManualApplicationRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public class DeployingCategory extends CreateCategory {
    public static final DeployingCategory INSTANCE = new DeployingCategory();

    public DeployingCategory() {
        super("deploying");
    }

    public static void register(ClientRecipeManager recipeManager, List<ReliableClientRecipe> output) {
        for (RecipeHolder<DeployerApplicationRecipe> entry : recipeManager.getRecipesForType(AllRecipeTypes.DEPLOYING)) {
            output.add(new DeployingView(entry.id().identifier(), entry.value()));
        }
        for (RecipeHolder<ManualApplicationRecipe> entry : recipeManager.getRecipesForType(AllRecipeTypes.ITEM_APPLICATION)) {
            output.add(new DeployingView(entry.id().identifier(), entry.value()));
        }
        for (RecipeHolder<SandPaperPolishingRecipe> entry : recipeManager.getRecipesForType(AllRecipeTypes.SANDPAPER_POLISHING)) {
            output.add(new DeployingView(entry.id().identifier(), entry.value()));
        }
    }

    @Override
    public int getDisplayHeight() {
        return 70;
    }

    @Override
    public int getSlotCount() {
        return 6;
    }

    @Override
    public ItemStack getIcon() {
        return AllItems.DEPLOYER.getDefaultInstance();
    }

    @Override
    public List<ItemStack> getCraftReferences() {
        return List.of(
            AllItems.DEPLOYER.getDefaultInstance(),
            AllItems.DEPOT.getDefaultInstance(),
            AllItems.BELT_CONNECTOR.getDefaultInstance()
        );
    }
}
