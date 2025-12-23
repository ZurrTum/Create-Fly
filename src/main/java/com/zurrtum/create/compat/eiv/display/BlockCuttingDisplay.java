package com.zurrtum.create.compat.eiv.display;

import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.compat.eiv.CreateDisplay;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import de.crafty.eiv.common.api.recipe.EivRecipeType;
import de.crafty.eiv.common.api.recipe.IEivServerRecipe;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraft.core.HolderSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BlockCuttingDisplay extends CreateDisplay {
    public List<ItemStack> ingredient;
    public List<List<ItemStack>> results;

    public BlockCuttingDisplay() {
    }

    public BlockCuttingDisplay(Ingredient ingredient, List<List<ItemStack>> results) {
        this.ingredient = getItemStacks(ingredient);
        this.results = results;
    }

    public static void register(List<IEivServerRecipe> recipes, RecipeMap preparedRecipes) {
        Object2ObjectMap<Ingredient, List<ItemStack>> map = new Object2ObjectOpenCustomHashMap<>(new Hash.Strategy<>() {
            public boolean equals(Ingredient ingredient, Ingredient other) {
                return Objects.equals(ingredient, other);
            }

            public int hashCode(Ingredient ingredient) {
                if (ingredient.values instanceof HolderSet.Direct<Item> direct) {
                    return direct.hashCode();
                }
                if (ingredient.values instanceof HolderSet.Named<Item> named) {
                    return named.key().location().hashCode();
                }
                return ingredient.hashCode();
            }
        });
        for (RecipeHolder<StonecutterRecipe> entry : preparedRecipes.byType(RecipeType.STONECUTTING)) {
            if (AllRecipeTypes.shouldIgnoreInAutomation(entry)) {
                continue;
            }
            StonecutterRecipe recipe = entry.value();
            map.computeIfAbsent(recipe.input(), i -> new ArrayList<>()).add(recipe.result());
        }
        for (Object2ObjectMap.Entry<Ingredient, List<ItemStack>> entry : map.object2ObjectEntrySet()) {
            List<ItemStack> outputs = entry.getValue();
            int size = outputs.size();
            if (size <= 15) {
                recipes.add(new BlockCuttingDisplay(entry.getKey(), outputs.stream().map(List::of).toList()));
                continue;
            }
            List<List<ItemStack>> list = new ArrayList<>(15);
            for (int i = 0; i < 15; i++) {
                List<ItemStack> stacks = new ArrayList<>(2);
                stacks.add(outputs.get(i));
                list.add(stacks);
            }
            for (int i = 15; i < size; i++) {
                list.get(i % 15).add(outputs.get(i));
            }
            recipes.add(new BlockCuttingDisplay(entry.getKey(), list));
        }
    }

    @Override
    public void writeToTag(CompoundTag tag) {
        RegistryOps<Tag> ops = getServerOps();
        tag.store("ingredient", STACKS_CODEC, ops, ingredient);
        tag.store("results", STACKS_LIST_CODEC, ops, results);
    }

    @Override
    public void loadFromTag(CompoundTag tag) {
        RegistryOps<Tag> ops = getClientOps();
        ingredient = tag.read("ingredient", STACKS_CODEC, ops).orElseThrow();
        results = tag.read("results", STACKS_LIST_CODEC, ops).orElseThrow();
    }

    @Override
    public EivRecipeType<BlockCuttingDisplay> getRecipeType() {
        return EivCommonPlugin.BLOCK_CUTTING;
    }
}
