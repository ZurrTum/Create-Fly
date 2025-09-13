package com.zurrtum.create.foundation.fluid;

import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;

import java.util.Optional;

public class FluidTank implements FluidInventory {
    protected FluidStack fluid = FluidStack.EMPTY;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected Optional<Integer> max;
    protected int capacity;

    public FluidTank(int capacity) {
        this.max = Optional.of(capacity);
        this.capacity = capacity;
    }

    public FluidStack getFluid() {
        return fluid;
    }

    public void setFluid(FluidStack fluid) {
        if (fluid != FluidStack.EMPTY) {
            setMaxSize(fluid, max);
        }
        this.fluid = fluid;
    }

    @Override
    public FluidStack onExtract(FluidStack stack) {
        return removeMaxSize(stack, max);
    }

    @Override
    public int getMaxAmountPerStack() {
        return capacity;
    }

    @Override
    public FluidStack getStack(int slot) {
        if (slot != 0) {
            return FluidStack.EMPTY;
        }
        return fluid;
    }

    @Override
    public void setStack(int slot, FluidStack stack) {
        if (slot == 0) {
            setFluid(stack);
        }
    }

    @Override
    public boolean isEmpty() {
        return fluid.isEmpty();
    }

    public void read(ReadView view) {
        fluid = view.read("Fluid", FluidStack.CODEC).orElse(FluidStack.EMPTY);
        if (fluid.getAmount() > capacity) {
            fluid.setAmount(capacity);
        }
    }

    public void setCapacity(int capacity) {
        this.max = Optional.of(capacity);
        this.capacity = capacity;
        if (!fluid.isEmpty()) {
            setMaxSize(fluid, max);
        }
    }

    @Override
    public int size() {
        return 1;
    }

    public void write(WriteView view) {
        if (!fluid.isEmpty()) {
            view.put("Fluid", FluidStack.CODEC, fluid);
        }
    }
}
