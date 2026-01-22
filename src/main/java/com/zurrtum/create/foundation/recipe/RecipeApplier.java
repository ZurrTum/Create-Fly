package com.zurrtum.create.foundation.recipe;

import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.infrastructure.items.BaseInventory;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class RecipeApplier {
    public static <T extends RecipeInput> void applyCreateRecipeOn(
        ItemEntity entity,
        T input,
        CreateRecipe<T> recipe,
        boolean returnProcessingRemainder
    ) {
        Level world = entity.level();
        List<ItemStack> stacks = applyCreateRecipeOn(entity.level(), entity.getItem().getCount(), input, recipe, returnProcessingRemainder);
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

    public static <T extends RecipeInput> List<ItemStack> applyCreateRecipeOn(
        Level world,
        int count,
        T input,
        CreateRecipe<T> recipe,
        boolean returnProcessingRemainder
    ) {
        List<ItemStack> remainders;
        if (returnProcessingRemainder) {
            remainders = new ArrayList<>();
            for (int i = 0, size = input.size(); i < size; i++) {
                ItemStackTemplate recipeRemainder = input.getItem(i).getItem().getCraftingRemainder();
                if (recipeRemainder == null) {
                    continue;
                }
                remainders.add(recipeRemainder.create());
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
                updateBuffer(buffer, recipe.assemble(input, world.getRandom()), stacks);
                if (remainders != null) {
                    updateBuffer(buffer, remainders, stacks);
                }
            }
            buffer.values().stream().map(ObjectIntPair::left).filter(stack -> !stack.isEmpty()).forEach(stacks::add);
            return stacks;
        } else {
            List<ItemStack> craft = recipe.assemble(input, world.getRandom());
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
                buffer.put(stack, ObjectIntPair.of(stack, stack.getMaxStackSize()));
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
        Level level,
        int count,
        T input,
        RecipeHolder<? extends Recipe<T>> entry,
        boolean returnProcessingRemainder
    ) {
        Recipe<T> recipe = entry.value();
        if (recipe instanceof CreateRecipe<T> createRecipe) {
            return applyCreateRecipeOn(level, count, input, createRecipe, returnProcessingRemainder);
        } else {
            return applyRecipeOn(count, input, recipe, returnProcessingRemainder);
        }
    }

    public static <T extends RecipeInput> List<ItemStack> applyRecipeOn(int count, T input, Recipe<T> recipe, boolean returnProcessingRemainder) {
        ItemStack result = recipe.assemble(input);
        if (returnProcessingRemainder) {
            int size = input.size();
            if (size != 1) {
                List<ItemStack> list = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    ItemStackTemplate recipeRemainder = input.getItem(i).getItem().getCraftingRemainder();
                    if (recipeRemainder == null) {
                        continue;
                    }
                    list.add(recipeRemainder.create());
                }
                list.add(result);
                return ItemHelper.multipliedOutput(list, count);
            }
            ItemStackTemplate recipeRemainder = input.getItem(0).getItem().getCraftingRemainder();
            if (recipeRemainder != null) {
                return ItemHelper.multipliedOutput(List.of(result, recipeRemainder.create()), count);
            }
        }
        return ItemHelper.multipliedOutput(result, count);
    }
}
