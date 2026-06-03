package com.zurrtum.create.client.compat.rrv.category;

import cc.cassian.rrv.api.recipe.ReliableClientRecipe;
import cc.cassian.rrv.client.recipe.ClientRecipeManager;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.client.compat.rrv.CreateCategory;
import com.zurrtum.create.client.compat.rrv.view.FanSmokingView;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmokingRecipe;

import java.util.List;

public class FanSmokingCategory extends CreateCategory {
    public static final FanSmokingCategory INSTANCE = new FanSmokingCategory();

    public static void register(ClientRecipeManager recipeManager, List<ReliableClientRecipe> output) {
        for (RecipeHolder<SmokingRecipe> entry : recipeManager.getRecipesForType(RecipeType.SMOKING)) {
            if (AllRecipeTypes.CAN_BE_AUTOMATED.test(entry)) {
                output.add(new FanSmokingView(entry.id().identifier(), entry.value()));
            }
        }
    }

    public FanSmokingCategory() {
        super("fan_smoking");
    }

    @Override
    public Component getDisplayName() {
        return CreateLang.translateDirect("recipe.fan_smoking");
    }

    @Override
    public int getDisplayHeight() {
        return 72;
    }

    @Override
    public int getSlotCount() {
        return 2;
    }

    @Override
    public ItemStack getIcon() {
        return AllItems.PROPELLER.getDefaultInstance();
    }

    @Override
    public ItemStack getSubIcon() {
        return Items.CAMPFIRE.getDefaultInstance();
    }

    @Override
    public List<ItemStack> getCraftReferences() {
        return List.of(AllItems.ENCASED_FAN.getDefaultInstance());
    }
}
