package com.zurrtum.create.compat.eiv.display;

import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.compat.eiv.CreateDisplay;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import de.crafty.eiv.common.api.recipe.EivRecipeType;
import de.crafty.eiv.common.api.recipe.IEivServerRecipe;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.recipe.*;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.entry.RegistryEntryList;

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

    public static void register(List<IEivServerRecipe> recipes, PreparedRecipes preparedRecipes) {
        Object2ObjectMap<Ingredient, List<ItemStack>> map = new Object2ObjectOpenCustomHashMap<>(new Hash.Strategy<>() {
            public boolean equals(Ingredient ingredient, Ingredient other) {
                return Objects.equals(ingredient, other);
            }

            public int hashCode(Ingredient ingredient) {
                if (ingredient.entries instanceof RegistryEntryList.Direct<Item> direct) {
                    return direct.hashCode();
                }
                if (ingredient.entries instanceof RegistryEntryList.Named<Item> named) {
                    return named.getTag().id().hashCode();
                }
                return ingredient.hashCode();
            }
        });
        for (RecipeEntry<StonecuttingRecipe> entry : preparedRecipes.getAll(RecipeType.STONECUTTING)) {
            if (AllRecipeTypes.shouldIgnoreInAutomation(entry)) {
                continue;
            }
            StonecuttingRecipe recipe = entry.value();
            map.computeIfAbsent(recipe.ingredient(), i -> new ArrayList<>()).add(recipe.result());
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
    public void writeToTag(NbtCompound tag) {
        RegistryOps<NbtElement> ops = getServerOps();
        tag.put("ingredient", STACKS_CODEC, ops, ingredient);
        tag.put("results", STACKS_LIST_CODEC, ops, results);
    }

    @Override
    public void loadFromTag(NbtCompound tag) {
        RegistryOps<NbtElement> ops = getClientOps();
        ingredient = tag.get("ingredient", STACKS_CODEC, ops).orElseThrow();
        results = tag.get("results", STACKS_LIST_CODEC, ops).orElseThrow();
    }

    @Override
    public EivRecipeType<BlockCuttingDisplay> getRecipeType() {
        return EivCommonPlugin.BLOCK_CUTTING;
    }
}
