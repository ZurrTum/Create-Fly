package com.zurrtum.create.infrastructure.transfer;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;

public abstract class SingleFluidStackStorage extends SnapshotParticipant<FluidStack> implements SingleSlotStorage<FluidVariant> {
    protected abstract FluidStack getStack();

    protected abstract void setStack(FluidStack stack);

    protected boolean canInsert(FluidVariant variant) {
        return true;
    }

    protected boolean canExtract(FluidVariant variant) {
        return true;
    }

    public int getCapacity(FluidVariant insert, FluidStack stack) {
        return stack.getMaxAmount();
    }

    @Override
    public boolean isResourceBlank() {
        return getStack().isEmpty();
    }

    @Override
    public FluidVariant getResource() {
        FluidStack stack = getStack();
        return FluidVariant.of(stack.getFluid(), stack.getComponentChanges());
    }

    @Override
    public long getAmount() {
        return getStack().getAmount();
    }

    @Override
    public long getCapacity() {
        return getStack().getMaxAmount();
    }

    @Override
    public long insert(FluidVariant insertedVariant, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(insertedVariant, maxAmount);

        FluidStack currentStack = getStack();

        if ((FluidInventoryStorage.matches(insertedVariant, currentStack) || currentStack.isEmpty()) && canInsert(insertedVariant)) {
            int insertedAmount = (int) Math.min(maxAmount, getCapacity(insertedVariant, currentStack) - currentStack.getAmount());

            if (insertedAmount > 0) {
                updateSnapshots(transaction);
                currentStack = getStack();

                if (currentStack.isEmpty()) {
                    Integer capacity = currentStack.get(AllDataComponents.FLUID_MAX_CAPACITY);
                    currentStack = new FluidStack(insertedVariant.getFluid(), insertedAmount, insertedVariant.getComponents());
                    if (capacity != null) {
                        currentStack.set(AllDataComponents.FLUID_MAX_CAPACITY, capacity);
                    }
                } else {
                    currentStack.increment(insertedAmount);
                }

                setStack(currentStack);

                return insertedAmount;
            }
        }

        return 0;
    }

    @Override
    public long extract(FluidVariant variant, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(variant, maxAmount);

        FluidStack currentStack = getStack();

        if (FluidInventoryStorage.matches(variant, currentStack) && canExtract(variant)) {
            int extracted = (int) Math.min(currentStack.getAmount(), maxAmount);

            if (extracted > 0) {
                this.updateSnapshots(transaction);
                currentStack = getStack();
                currentStack.decrement(extracted);
                setStack(currentStack);

                return extracted;
            }
        }

        return 0;
    }

    @Override
    protected FluidStack createSnapshot() {
        FluidStack original = getStack();
        setStack(original.copy());
        return original;
    }

    @Override
    protected void readSnapshot(FluidStack snapshot) {
        setStack(snapshot);
    }

    @Override
    public String toString() {
        return "SingleFluidStackStorage[" + getStack() + "]";
    }
}
