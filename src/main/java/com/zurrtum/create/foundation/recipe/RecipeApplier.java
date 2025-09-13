package com.zurrtum.create.foundation.recipe;

import com.zurrtum.create.foundation.item.ItemHelper;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RecipeApplier {
    public static <T extends RecipeInput> void applyCreateRecipeOn(ItemEntity entity, T input, CreateRecipe<T> recipe) {
        World world = entity.getWorld();
        List<ItemStack> stacks = applyCreateRecipeOn(entity.getWorld(), entity.getStack().getCount(), input, recipe);
        int size = stacks.size();
        if (size == 0) {
            entity.discard();
            return;
        }
        entity.setStack(stacks.getFirst());
        if (size == 1) {
            return;
        }
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();
        Vec3d velocity = entity.getVelocity();
        for (int i = 1; i < size; i++) {
            ItemEntity entityIn = new ItemEntity(world, x, y, z, stacks.get(i));
            entityIn.setVelocity(velocity);
            world.spawnEntity(entityIn);
        }
    }

    public static <T extends RecipeInput> List<ItemStack> applyCreateRecipeOn(World world, int count, T input, CreateRecipe<T> recipe) {
        if (recipe.isRollable()) {
            List<ItemStack> stacks = new ArrayList<>();
            HashMap<ItemStack, Pair<ItemStack, Integer>> buffer = new HashMap<>();
            ItemStack exist;
            for (int i = 0, amount, max; i < count; i++) {
                for (ItemStack stack : recipe.craft(input, world.random)) {
                    Pair<ItemStack, Integer> item = buffer.get(stack);
                    if (item == null) {
                        buffer.put(stack, new Pair<>(stack, stack.getMaxCount()));
                    } else {
                        max = item.getRight();
                        exist = item.getLeft();
                        amount = stack.getCount() + exist.getCount();
                        if (amount >= max) {
                            exist.setCount(amount - max);
                            stack.setCount(max);
                            stacks.add(stack);
                        } else {
                            exist.setCount(amount);
                        }
                    }
                }
            }
            buffer.values().stream().map(Pair::getLeft).filter(stack -> !stack.isEmpty()).forEach(stacks::add);
            return stacks;
        } else {
            return ItemHelper.multipliedOutput(recipe.craft(input, world.random), count);
        }
    }

    public static <T extends RecipeInput> List<ItemStack> applyRecipeOn(World level, int count, T input, RecipeEntry<? extends Recipe<T>> entry) {
        Recipe<T> recipe = entry.value();
        if (recipe instanceof CreateRecipe<T> createRecipe) {
            return applyCreateRecipeOn(level, count, input, createRecipe);
        } else {
            return applyRecipeOn(level, count, input, recipe);
        }
    }

    public static <T extends RecipeInput> List<ItemStack> applyRecipeOn(World level, int count, T input, Recipe<T> recipe) {
        return ItemHelper.multipliedOutput(recipe.craft(input, level.getRegistryManager()), count);
    }
}
