package com.zurrtum.create.client.compat.rrv.category;

import cc.cassian.rrv.api.recipe.ReliableClientRecipe;
import cc.cassian.rrv.client.recipe.ClientRecipeManager;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.client.compat.rrv.CreateCategory;
import com.zurrtum.create.client.compat.rrv.view.MixingView;
import com.zurrtum.create.content.kinetics.mixer.MixingRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public class MixingCategory extends CreateCategory {
    public static final MixingCategory INSTANCE = new MixingCategory();

    public static void register(ClientRecipeManager recipeManager, List<ReliableClientRecipe> output) {
        for (RecipeHolder<MixingRecipe> entry : recipeManager.getRecipesForType(AllRecipeTypes.MIXING)) {
            output.add(new MixingView(entry.id().identifier(), entry.value()));
        }
    }

    public MixingCategory() {
        super("mixing");
    }

    @Override
    public int getDisplayHeight() {
        return 99;
    }

    @Override
    public int getSlotCount() {
        return 19;
    }

    @Override
    public ItemStack getIcon() {
        return AllItems.MECHANICAL_MIXER.getDefaultInstance();
    }

    @Override
    public ItemStack getSubIcon() {
        return AllItems.BASIN.getDefaultInstance();
    }

    @Override
    public List<ItemStack> getCraftReferences() {
        return List.of(AllItems.MECHANICAL_MIXER.getDefaultInstance(), AllItems.BASIN.getDefaultInstance());
    }
}
