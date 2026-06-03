package com.zurrtum.create.client.compat.rrv.category;

import cc.cassian.rrv.api.recipe.ReliableClientRecipe;
import cc.cassian.rrv.client.recipe.ClientRecipeManager;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.compat.rrv.CreateCategory;
import com.zurrtum.create.client.compat.rrv.view.BlockCuttingView;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.fabricmc.fabric.api.recipe.v1.ingredient.FabricIngredient;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.StonecutterRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BlockCuttingCategory extends CreateCategory {
    public static final BlockCuttingCategory INSTANCE = new BlockCuttingCategory();

    public static void register(ClientRecipeManager recipeManager, List<ReliableClientRecipe> output) {
        Object2ObjectMap<Ingredient, Pair<Identifier, List<ItemStack>>> map = new Object2ObjectOpenCustomHashMap<>(new Hash.Strategy<>() {
            public boolean equals(Ingredient ingredient, Ingredient other) {
                return Objects.equals(ingredient, other);
            }

            public int hashCode(Ingredient ingredient) {
                if (((FabricIngredient) ingredient).getCustomIngredient() != null) {
                    return ingredient.hashCode();
                }
                if (ingredient.values instanceof HolderSet.Direct<Item> direct) {
                    return direct.hashCode();
                }
                if (ingredient.values instanceof HolderSet.Named<Item> named) {
                    return named.key().location().hashCode();
                }
                return ingredient.hashCode();
            }
        });
        for (RecipeHolder<StonecutterRecipe> entry : recipeManager.getRecipesForType(RecipeType.STONECUTTING)) {
            if (AllRecipeTypes.shouldIgnoreInAutomation(entry)) {
                continue;
            }
            StonecutterRecipe recipe = entry.value();
            map.computeIfAbsent(recipe.input(), i -> Pair.of(entry.id().identifier(), new ArrayList<>())).getSecond()
                .add(recipe.result().create());
        }
        for (Object2ObjectMap.Entry<Ingredient, Pair<Identifier, List<ItemStack>>> entry : map.object2ObjectEntrySet()) {
            Pair<Identifier, List<ItemStack>> pair = entry.getValue();
            List<ItemStack> outputs = pair.getSecond();
            int size = outputs.size();
            if (size <= 15) {
                output.add(new BlockCuttingView(
                    pair.getFirst(),
                    entry.getKey(),
                    outputs.stream().map(List::of).toList()
                ));
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
            output.add(new BlockCuttingView(pair.getFirst(), entry.getKey(), list));
        }
    }

    public BlockCuttingCategory() {
        super("block_cutting");
    }

    @Override
    public int getDisplayHeight() {
        return 61;
    }

    @Override
    public int getSlotCount() {
        return 16;
    }

    @Override
    public ItemStack getIcon() {
        return AllItems.MECHANICAL_SAW.getDefaultInstance();
    }

    @Override
    public ItemStack getSubIcon() {
        return Items.STONE_BRICK_STAIRS.getDefaultInstance();
    }

    @Override
    public List<ItemStack> getCraftReferences() {
        return List.of(AllItems.MECHANICAL_SAW.getDefaultInstance());
    }
}
