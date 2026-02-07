package com.zurrtum.create.compat.eiv.display;

import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import com.zurrtum.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.zurrtum.create.content.kinetics.deployer.ItemApplicationRecipe;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import de.crafty.eiv.common.api.recipe.EivRecipeType;
import de.crafty.eiv.common.api.recipe.IEivServerRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DeployingDisplay extends ManualApplicationDisplay {
    public DeployingDisplay() {
    }

    public DeployingDisplay(List<ProcessingOutput> outputs, Ingredient target, Ingredient ingredient, boolean keepHeldItem) {
        int size = outputs.size();
        results = new ArrayList<>(size);
        chances = new ArrayList<>(size);
        for (ProcessingOutput output : outputs) {
            results.add(output.create());
            chances.add(output.chance());
        }
        this.target = getItemStacks(target);
        this.ingredient = getItemStacks(ingredient);
        this.keepHeldItem = keepHeldItem;
    }

    @Nullable
    public static DeployingDisplay of(RecipeEntry<?> entry) {
        if (!AllRecipeTypes.CAN_BE_AUTOMATED.test(entry)) {
            return null;
        }
        Recipe<?> recipe = entry.value();
        if (recipe instanceof ItemApplicationRecipe r) {
            return new DeployingDisplay(r.results(), r.target(), r.ingredient(), r.keepHeldItem());
        } else if (recipe instanceof SandPaperPolishingRecipe(ItemStack result, Ingredient target)) {
            List<RegistryEntry<Item>> sandpaperList = new ArrayList<>();
            for (RegistryEntry<Item> item : Registries.ITEM.iterateEntries(AllItemTags.SANDPAPER)) {
                sandpaperList.add(item);
            }
            Ingredient ingredient = Ingredient.ofTag(RegistryEntryList.of(sandpaperList));
            return new DeployingDisplay(List.of(new ProcessingOutput(result)), target, ingredient, false);
        }
        return null;
    }

    @Override
    public EivRecipeType<? extends IEivServerRecipe> getRecipeType() {
        return EivCommonPlugin.DEPLOYING;
    }
}
