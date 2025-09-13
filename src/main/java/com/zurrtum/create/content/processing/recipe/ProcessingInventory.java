package com.zurrtum.create.content.processing.recipe;

import com.zurrtum.create.infrastructure.transfer.SlotRangeCache;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ProcessingInventory implements SidedInventory {
    private static final int[] INPUT_SLOTS = {0};
    private static final int[] ALL_SLOTS = SlotRangeCache.get(32);
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static final Optional<Integer> LIMIT = Optional.of(1);
    public float remainingTime;
    public float recipeDuration;
    public boolean appliedRecipe;
    private boolean limit;
    private byte outputFlag = 0;
    private final Predicate<Direction> canInsert;
    private final Consumer<ItemStack> callback;
    private final DefaultedList<ItemStack> stacks;

    public ProcessingInventory(Consumer<ItemStack> callback, Predicate<Direction> canInsert) {
        this.stacks = DefaultedList.ofSize(32, ItemStack.EMPTY);
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
    public int size() {
        return 32;
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return outputFlag == 0 ? INPUT_SLOTS : ALL_SLOTS;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        if (outputFlag == 0) {
            return slot == 0 && canInsert.test(dir);
        }
        return slot != 0;
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        if (outputFlag == 0) {
            return isEmpty();
        }
        return true;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
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
    public ItemStack getStack(int slot) {
        if (slot >= 32) {
            return ItemStack.EMPTY;
        }
        return stacks.get(slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
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
    public int getMaxCountPerStack() {
        return limit ? 1 : SidedInventory.super.getMaxCountPerStack();
    }

    @Override
    public void clear() {
        remainingTime = 0;
        recipeDuration = 0;
        appliedRecipe = false;
        SidedInventory.super.clear();
    }

    public void write(WriteView view) {
        WriteView.ListAppender<ItemStack> list = view.getListAppender("Inventory", ItemStack.OPTIONAL_CODEC);
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

    public void read(ReadView view) {
        ReadView.TypedListReadView<ItemStack> list = view.getTypedListView("Inventory", ItemStack.OPTIONAL_CODEC);
        int i = 0;
        for (ItemStack itemStack : list) {
            stacks.set(i++, itemStack);
        }
        for (int size = stacks.size(); i < size; i++) {
            stacks.set(i, ItemStack.EMPTY);
        }
        remainingTime = view.getFloat("ProcessingTime", 0);
        recipeDuration = view.getFloat("RecipeTime", 0);
        appliedRecipe = view.getBoolean("AppliedRecipe", false);
        if (appliedRecipe && isEmpty())
            appliedRecipe = false;
    }
}
