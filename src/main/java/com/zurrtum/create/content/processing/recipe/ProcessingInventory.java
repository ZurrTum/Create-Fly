package com.zurrtum.create.content.processing.recipe;

import com.zurrtum.create.infrastructure.items.SidedItemInventory;
import com.zurrtum.create.infrastructure.transfer.SlotRangeCache;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ProcessingInventory implements SidedItemInventory {
    private static final int[] INPUT_SLOTS = {0};
    private static final int[] ALL_SLOTS = SlotRangeCache.get(32);
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static final Optional<Integer> LIMIT = Optional.of(1);
    public float remainingTime;
    public float recipeDuration;
    public boolean appliedRecipe;
    private boolean limit;
    private byte outputFlag = 0;
    private final Predicate<@Nullable Direction> canInsert;
    private final Consumer<ItemStack> callback;
    private final NonNullList<ItemStack> stacks;

    public ProcessingInventory(Consumer<ItemStack> callback, Predicate<@Nullable Direction> canInsert) {
        this.stacks = NonNullList.withSize(32, ItemStack.EMPTY);
        this.canInsert = canInsert;
        this.callback = callback;
    }

    public void outputAllowInsertion() {
        outputFlag = (byte) (limit ? 1 : 2);
        limit = false;
    }

    public void outputForbidInsertion() {
        limit = outputFlag == 1;
        outputFlag = 0;
    }

    @Override
    public int getContainerSize() {
        return 32;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return outputFlag == 0 ? INPUT_SLOTS : ALL_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
        if (outputFlag == 0) {
            return slot == 0 && canInsert.test(dir);
        }
        return slot != 0;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (outputFlag == 0) {
            return isEmpty();
        }
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return false;
    }

    @Override
    public ItemStack onExtract(ItemStack stack) {
        if (limit) {
            return removeMaxSize(stack, LIMIT);
        }
        return stack;
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot >= 32) {
            return ItemStack.EMPTY;
        }
        return stacks.get(slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot >= 32) {
            return;
        }
        if (limit && stack != ItemStack.EMPTY) {
            setMaxSize(stack, LIMIT);
        }
        stacks.set(slot, stack);
        if (slot == 0 && !stack.isEmpty()) {
            callback.accept(stack);
        }
    }

    public ProcessingInventory withSlotLimit(boolean limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public int getMaxStackSize() {
        return limit ? 1 : SidedItemInventory.super.getMaxStackSize();
    }

    @Override
    public void clearContent() {
        remainingTime = 0;
        recipeDuration = 0;
        appliedRecipe = false;
        SidedItemInventory.super.clearContent();
    }

    public void write(ValueOutput view) {
        ValueOutput.TypedOutputList<ItemStack> list = view.list("Inventory", ItemStack.OPTIONAL_CODEC);
        list.add(stacks.getFirst());
        for (int i = 1; i < stacks.size(); i++) {
            ItemStack stack = stacks.get(i);
            if (stack.isEmpty()) {
                continue;
            }
            list.add(stack);
        }
        view.putFloat("ProcessingTime", remainingTime);
        view.putFloat("RecipeTime", recipeDuration);
        view.putBoolean("AppliedRecipe", appliedRecipe);
    }

    public void read(ValueInput view) {
        ValueInput.TypedInputList<ItemStack> list = view.listOrEmpty("Inventory", ItemStack.OPTIONAL_CODEC);
        int i = 0;
        for (ItemStack itemStack : list) {
            stacks.set(i++, itemStack);
        }
        for (int size = stacks.size(); i < size; i++) {
            stacks.set(i, ItemStack.EMPTY);
        }
        remainingTime = view.getFloatOr("ProcessingTime", 0);
        recipeDuration = view.getFloatOr("RecipeTime", 0);
        appliedRecipe = view.getBooleanOr("AppliedRecipe", false);
        if (appliedRecipe && isEmpty())
            appliedRecipe = false;
    }
}
