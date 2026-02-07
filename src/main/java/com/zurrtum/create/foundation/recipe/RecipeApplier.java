package com.zurrtum.create.foundation.recipe;

import com.zurrtum.create.infrastructure.items.BaseInventory;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class RecipeApplier {
    public static <T extends RecipeInput> void applyRecipeOn(ItemEntity entity, T input, CreateRollableRecipe<T> recipe) {
        World world = entity.getWorld();
        List<ItemStack> stacks = applyRecipeOn(world.getRandom(), entity.getStack().getCount(), input, recipe);
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

    public static <T extends RecipeInput> List<ItemStack> applyRecipeOn(Random random, int count, T input, CreateRollableRecipe<T> recipe) {
        List<ItemStack> remainders = new ArrayList<>();
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
        List<ItemStack> stacks = new ArrayList<>();
        Object2ObjectMap<ItemStack, ObjectIntPair<ItemStack>> buffer = new Object2ObjectOpenCustomHashMap<>(BaseInventory.ITEM_STACK_HASH_STRATEGY);
        int max, amount;
        ItemStack exist;
        for (int i = 0; i < count; i++) {
            for (ItemStack stack : recipe.craft(input, random)) {
                if (stack.isEmpty()) {
                    continue;
                }
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
                        stacks.add(stack);
                    } else {
                        exist.setCount(amount);
                    }
                }
            }
            if (remainders != null) {
                for (ItemStack stack : remainders) {
                    ObjectIntPair<ItemStack> item = buffer.get(stack);
                    if (item == null) {
                        stack = stack.copy();
                        buffer.put(stack, ObjectIntPair.of(stack, stack.getMaxCount()));
                    } else {
                        max = item.rightInt();
                        exist = item.left();
                        amount = stack.getCount() + exist.getCount();
                        if (amount >= max) {
                            exist.setCount(amount - max);
                            stack = stack.copy();
                            stack.setCount(max);
                            stacks.add(stack);
                        } else {
                            exist.setCount(amount);
                        }
                    }
                }
            }
        }
        buffer.values().stream().map(ObjectIntPair::left).forEach(stacks::add);
        return stacks;
    }
}
