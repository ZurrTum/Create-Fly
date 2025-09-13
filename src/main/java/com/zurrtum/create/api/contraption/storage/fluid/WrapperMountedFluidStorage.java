package com.zurrtum.create.api.contraption.storage.fluid;

import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;

/**
 * Partial implementation of a MountedFluidStorage that wraps a fluid handler.
 */
public abstract class WrapperMountedFluidStorage<T extends FluidInventory> extends MountedFluidStorage {
    protected T wrapped;

    protected WrapperMountedFluidStorage(MountedFluidStorageType<?> type) {
        super(type);
    }

    protected WrapperMountedFluidStorage(MountedFluidStorageType<?> type, T wrapped) {
        super(type);
        this.wrapped = wrapped;
    }

    @Override
    public int size() {
        return wrapped.size();
    }

    @Override
    public FluidStack getStack(int slot) {
        return wrapped.getStack(slot);
    }

    @Override
    public void setStack(int slot, FluidStack stack) {
        wrapped.setStack(slot, stack);
    }

    @Override
    public int getMaxAmountPerStack() {
        return wrapped.getMaxAmountPerStack();
    }

    @Override
    public int getMaxAmount(FluidStack stack) {
        return wrapped.getMaxAmount(stack);
    }

    @Override
    public int insert(FluidStack stack) {
        return wrapped.insert(stack);
    }

    @Override
    public int extract(FluidStack stack) {
        return wrapped.extract(stack);
    }
}