package com.zurrtum.create.client.compat.rrv.category;

import cc.cassian.rrv.api.recipe.ReliableClientRecipe;
import cc.cassian.rrv.client.recipe.ClientRecipeManager;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.client.compat.rrv.CreateCategory;
import com.zurrtum.create.client.compat.rrv.view.FanWashingView;
import com.zurrtum.create.content.kinetics.fan.processing.SplashingRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public class FanWashingCategory extends CreateCategory {
    public static final FanWashingCategory INSTANCE = new FanWashingCategory();

    public static void register(ClientRecipeManager recipeManager, List<ReliableClientRecipe> output) {
        for (RecipeHolder<SplashingRecipe> entry : recipeManager.getRecipesForType(AllRecipeTypes.SPLASHING)) {
            output.add(new FanWashingView(entry.id().identifier(), entry.value()));
        }
    }

    public FanWashingCategory() {
        super("fan_washing");
    }

    @Override
    public int getDisplayHeight() {
        return 72;
    }

    @Override
    public int getSlotCount() {
        return 13;
    }

    @Override
    public ItemStack getIcon() {
        return AllItems.PROPELLER.getDefaultInstance();
    }

    @Override
    public ItemStack getSubIcon() {
        return Items.WATER_BUCKET.getDefaultInstance();
    }

    @Override
    public List<ItemStack> getCraftReferences() {
        return List.of(AllItems.ENCASED_FAN.getDefaultInstance());
    }
}
