package com.zurrtum.create.infrastructure.transfer;

import com.zurrtum.create.infrastructure.fluids.SidedFluidInventory;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.util.math.Direction;

public class SidedFluidInventorySlotWrapper implements SingleSlotStorage<FluidVariant> {
    private final FluidInventorySlotWrapper slotWrapper;
    private final SidedFluidInventory sidedInventory;
    private final Direction direction;

    SidedFluidInventorySlotWrapper(FluidInventorySlotWrapper slotWrapper, SidedFluidInventory sidedInventory, Direction direction) {
        this.slotWrapper = slotWrapper;
        this.sidedInventory = sidedInventory;
        this.direction = direction;
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        if (!sidedInventory.canInsert(slotWrapper.slot, FluidInventoryStorage.getCachedStack(resource), direction)) {
            return 0;
        } else {
            return slotWrapper.insert(resource, maxAmount, transaction);
        }
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        if (!sidedInventory.canExtract(slotWrapper.slot, FluidInventoryStorage.getCachedStack(resource), direction)) {
            return 0;
        } else {
            return slotWrapper.extract(resource, maxAmount, transaction);
        }
    }

    @Override
    public boolean isResourceBlank() {
        return slotWrapper.isResourceBlank();
    }

    @Override
    public FluidVariant getResource() {
        return slotWrapper.getResource();
    }

    @Override
    public long getAmount() {
        return slotWrapper.getAmount();
    }

    @Override
    public long getCapacity() {
        return slotWrapper.getCapacity();
    }

    @Override
    public StorageView<FluidVariant> getUnderlyingView() {
        return slotWrapper.getUnderlyingView();
    }

    @Override
    public String toString() {
        return "SidedInventorySlotWrapper[%s#%d/%s]".formatted(FluidInventoryStorage.toString(sidedInventory), slotWrapper.slot, direction.name());
    }
}
