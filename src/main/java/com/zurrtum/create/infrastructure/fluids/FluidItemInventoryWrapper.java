package com.zurrtum.create.infrastructure.fluids;

import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.stream.Stream;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public abstract class FluidItemInventoryWrapper implements FluidItemInventory {
    public Consumer<FluidItemInventoryWrapper> release;
    public ItemStack stack;

    @Override
    public void close() {
        stack = ItemStack.EMPTY;
        release.accept(this);
    }

    @Override
    public ItemStack getContainer() {
        return stack;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return stack.isEmpty();
    }

    abstract public FluidStack getStack();

    abstract public void setStack(FluidStack stack);

    public boolean canInsert() {
        return true;
    }

    public boolean canExtract() {
        return true;
    }

    @Override
    public int insert(FluidStack stack) {
        if (!canInsert()) {
            return 0;
        }
        return FluidItemInventory.super.insert(stack);
    }

    @Override
    public boolean preciseInsert(FluidStack stack) {
        if (!canInsert()) {
            return false;
        }
        return FluidItemInventory.super.preciseInsert(stack);
    }

    @Override
    public int extract(FluidStack stack) {
        if (!canExtract()) {
            return 0;
        }
        return FluidItemInventory.super.extract(stack);
    }

    @Override
    public boolean preciseExtract(FluidStack stack) {
        if (!canExtract()) {
            return false;
        }
        return FluidItemInventory.super.preciseExtract(stack);
    }

    public FluidStack removeStack() {
        FluidStack stack = getStack();
        if (stack.isEmpty()) {
            return FluidStack.EMPTY;
        }
        setStack(FluidStack.EMPTY);
        return stack;
    }

    public FluidStack removeStackWithAmount(int amount) {
        FluidStack stack = getStack();
        if (stack.isEmpty()) {
            return FluidStack.EMPTY;
        }
        FluidStack result = stack.split(amount);
        setStack(stack);
        return result;
    }

    @Override
    public FluidStack removeStack(int slot) {
        if (slot != 0) {
            return FluidStack.EMPTY;
        }
        if (!canExtract()) {
            return FluidStack.EMPTY;
        }
        return removeStack();
    }

    @Override
    public FluidStack removeStack(int slot, int amount) {
        if (slot != 0) {
            return FluidStack.EMPTY;
        }
        if (!canExtract()) {
            return FluidStack.EMPTY;
        }
        return removeStackWithAmount(amount);
    }

    @Override
    public FluidStack getStack(int slot) {
        if (slot != 0) {
            return FluidStack.EMPTY;
        }
        return getStack();
    }

    @Override
    public void setStack(int slot, FluidStack stack) {
        if (slot != 0) {
            return;
        }
        setStack(stack);
    }

    @Override
    public Stream<FluidStack> stream(Direction side) {
        return stream();
    }

    @Override
    public Stream<FluidStack> stream() {
        return canExtract() ? Stream.of(getStack()) : Stream.empty();
    }

    @NotNull
    public java.util.Iterator<FluidStack> iterator() {
        return new com.zurrtum.create.infrastructure.fluids.FluidItemInventoryWrapper.Iterator(this);
    }

    public static class Iterator implements java.util.Iterator<FluidStack> {
        private FluidStack stack;
        private boolean hasNext;

        public Iterator(FluidItemInventoryWrapper inventory) {
            if (inventory.canExtract()) {
                stack = inventory.getStack();
                hasNext = true;
            } else {
                hasNext = false;
            }
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public FluidStack next() {
            if (!hasNext) {
                throw new NoSuchElementException();
            }
            hasNext = false;
            return stack;
        }
    }
}
