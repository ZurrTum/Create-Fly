package com.zurrtum.create.foundation.recipe;

import com.zurrtum.create.infrastructure.items.BaseInventory;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RecipeApplier {
    public static <T extends RecipeInput> void applyRecipeOn(ItemEntity entity, T input, CreateRollableRecipe<T> recipe) {
        Level world = entity.level();
        List<ItemStack> stacks = applyRecipeOn(world.getRandom(), entity.getItem().getCount(), input, recipe);
        int size = stacks.size();
        if (size == 0) {
            entity.discard();
            return;
        }
        entity.setItem(stacks.getFirst());
        if (size == 1) {
            return;
        }
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();
        Vec3 velocity = entity.getDeltaMovement();
        for (int i = 1; i < size; i++) {
            ItemEntity entityIn = new ItemEntity(world, x, y, z, stacks.get(i));
            entityIn.setDeltaMovement(velocity);
            world.addFreshEntity(entityIn);
        }
    }

    public static <T extends RecipeInput> List<ItemStack> applyRecipeOn(RandomSource random, int count, T input, CreateRollableRecipe<T> recipe) {
        List<ItemStack> remainders;
        remainders = new ArrayList<>();
        for (int i = 0, size = input.size(); i < size; i++) {
            ItemStackTemplate recipeRemainder = input.getItem(i).getItem().getCraftingRemainder();
            if (recipeRemainder == null) {
                continue;
            }
            ItemStack stack = recipeRemainder.create();
            if (stack.isEmpty()) {
                continue;
            }
            remainders.add(stack);
        }
        if (remainders.isEmpty()) {
            remainders = null;
        }
        List<ItemStack> stacks = new ArrayList<>();
        Object2ObjectMap<ItemStack, @Nullable ObjectIntPair<ItemStack>> buffer = new Object2ObjectOpenCustomHashMap<>(BaseInventory.ITEM_STACK_HASH_STRATEGY);
        int max, amount;
        ItemStack exist;
        for (int i = 0; i < count; i++) {
            for (ItemStack stack : recipe.assemble(input, random)) {
                if (stack.isEmpty()) {
                    continue;
                }
                ObjectIntPair<ItemStack> item = buffer.get(stack);
                if (item == null) {
                    buffer.put(stack, ObjectIntPair.of(stack, stack.getMaxStackSize()));
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
                        buffer.put(stack, ObjectIntPair.of(stack, stack.getMaxStackSize()));
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
