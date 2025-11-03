package com.zurrtum.create.foundation.recipe;

import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.infrastructure.items.BaseInventory;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class RecipeApplier {
    public static <T extends RecipeInput> void applyCreateRecipeOn(
        ItemEntity entity,
        T input,
        CreateRecipe<T> recipe,
        boolean returnProcessingRemainder
    ) {
        World world = entity.getEntityWorld();
        List<ItemStack> stacks = applyCreateRecipeOn(entity.getEntityWorld(), entity.getStack().getCount(), input, recipe, returnProcessingRemainder);
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

    public static <T extends RecipeInput> List<ItemStack> applyCreateRecipeOn(
        World world,
        int count,
        T input,
        CreateRecipe<T> recipe,
        boolean returnProcessingRemainder
    ) {
        List<ItemStack> remainders;
        if (returnProcessingRemainder) {
            remainders = new ArrayList<>();
            for (int i = 0, size = input.size(); i < size; i++) {
                ItemStack recipeRemainder = input.getStackInSlot(i).getItem().getRecipeRemainder();
                if (recipeRemainder.isEmpty()) {
                    continue;
                }
                remainders.add(recipeRemainder);
            }
            if (remainders.isEmpty()) {
                remainders = null;
            }
        } else {
            remainders = null;
        }
        if (recipe.isRollable()) {
            List<ItemStack> stacks = new ArrayList<>();
            Object2ObjectMap<ItemStack, ObjectIntPair<ItemStack>> buffer = new Object2ObjectOpenCustomHashMap<>(BaseInventory.ITEM_STACK_HASH_STRATEGY);
            for (int i = 0; i < count; i++) {
                updateBuffer(buffer, recipe.craft(input, world.random), stacks);
                if (remainders != null) {
                    updateBuffer(buffer, remainders, stacks);
                }
            }
            buffer.values().stream().map(ObjectIntPair::left).filter(stack -> !stack.isEmpty()).forEach(stacks::add);
            return stacks;
        } else {
            List<ItemStack> craft = recipe.craft(input, world.random);
            if (remainders != null) {
                remainders.addAll(craft);
                return ItemHelper.multipliedOutput(remainders, count);
            } else {
                return ItemHelper.multipliedOutput(craft, count);
            }
        }
    }

    private static void updateBuffer(Object2ObjectMap<ItemStack, ObjectIntPair<ItemStack>> buffer, List<ItemStack> insert, List<ItemStack> outputs) {
        int max, amount;
        ItemStack exist;
        for (ItemStack stack : insert) {
            ObjectIntPair<ItemStack> item = buffer.get(stack);
            if (item == null) {
                buffer.put(stack, ObjectIntPair.of(stack, stack.getMaxCount()));
            } else {
                max = item.rightInt();
                exist = item.left();
                amount = stack.getCount() + exist.getCount();
                if (amount >= max) {
                    exist.setCount(amount - max);
                    stack.setCount(max);
                    outputs.add(stack);
                } else {
                    exist.setCount(amount);
                }
            }
        }
    }

    public static <T extends RecipeInput> List<ItemStack> applyRecipeOn(
        World level,
        int count,
        T input,
        RecipeEntry<? extends Recipe<T>> entry,
        boolean returnProcessingRemainder
    ) {
        Recipe<T> recipe = entry.value();
        if (recipe instanceof CreateRecipe<T> createRecipe) {
            return applyCreateRecipeOn(level, count, input, createRecipe, returnProcessingRemainder);
        } else {
            return applyRecipeOn(level, count, input, recipe, returnProcessingRemainder);
        }
    }

    public static <T extends RecipeInput> List<ItemStack> applyRecipeOn(
        World level,
        int count,
        T input,
        Recipe<T> recipe,
        boolean returnProcessingRemainder
    ) {
        ItemStack result = recipe.craft(input, level.getRegistryManager());
        if (returnProcessingRemainder) {
            int size = input.size();
            if (size != 1) {
                List<ItemStack> list = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    ItemStack recipeRemainder = input.getStackInSlot(i).getItem().getRecipeRemainder();
                    if (recipeRemainder.isEmpty()) {
                        continue;
                    }
                    list.add(recipeRemainder);
                }
                list.add(result);
                return ItemHelper.multipliedOutput(list, count);
            }
            ItemStack recipeRemainder = input.getStackInSlot(0).getItem().getRecipeRemainder();
            if (!recipeRemainder.isEmpty()) {
                return ItemHelper.multipliedOutput(List.of(result, recipeRemainder), count);
            }
        }
        return ItemHelper.multipliedOutput(result, count);
    }
}
