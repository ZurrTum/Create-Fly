package com.zurrtum.create.compat.eiv.display;

import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import com.zurrtum.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.zurrtum.create.content.kinetics.deployer.ItemApplicationRecipe;
import de.crafty.eiv.common.api.recipe.EivRecipeType;
import de.crafty.eiv.common.api.recipe.IEivServerRecipe;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DeployingDisplay extends ManualApplicationDisplay {
    public DeployingDisplay() {
    }

    public DeployingDisplay(ItemStack result, Ingredient target, Ingredient ingredient, boolean keepHeldItem) {
        this.result = result;
        this.target = getItemStacks(target);
        this.ingredient = getItemStacks(ingredient);
        this.keepHeldItem = keepHeldItem;
    }

    @Nullable
    public static DeployingDisplay of(RecipeHolder<?> entry) {
        if (!AllRecipeTypes.CAN_BE_AUTOMATED.test(entry)) {
            return null;
        }
        Recipe<?> recipe = entry.value();
        if (recipe instanceof ItemApplicationRecipe r) {
            return new DeployingDisplay(r.result(), r.target(), r.ingredient(), r.keepHeldItem());
        } else if (recipe instanceof SandPaperPolishingRecipe(ItemStack result, Ingredient target)) {
            List<Holder<Item>> sandpaperList = new ArrayList<>();
            for (Holder<Item> item : BuiltInRegistries.ITEM.getTagOrEmpty(AllItemTags.SANDPAPER)) {
                sandpaperList.add(item);
            }
            Ingredient ingredient = Ingredient.of(HolderSet.direct(sandpaperList));
            return new DeployingDisplay(result, target, ingredient, false);
        }
        return null;
    }

    @Override
    public EivRecipeType<? extends IEivServerRecipe> getRecipeType() {
        return EivCommonPlugin.DEPLOYING;
    }
}
