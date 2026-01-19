package com.zurrtum.create.infrastructure.transfer;

import com.google.common.collect.MapMaker;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.SidedFluidInventory;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FluidInventoryStorageImpl extends CombinedStorage<FluidVariant, SingleSlotStorage<FluidVariant>> implements FluidInventoryStorage {
    private static final Map<FluidInventory, FluidInventoryStorageImpl> WRAPPERS = new MapMaker().weakValues().makeMap();

    public static FluidInventoryStorage of(FluidInventory inventory) {
        FluidInventoryStorageImpl storage = WRAPPERS.computeIfAbsent(inventory, FluidInventoryStorageImpl::new);
        storage.resizeSlotList();
        return storage;
    }

    public static FluidInventoryStorage of(FluidInventory inventory, @Nullable Direction direction) {
        FluidInventoryStorageImpl storage = WRAPPERS.computeIfAbsent(inventory, FluidInventoryStorageImpl::new);
        storage.resizeSlotList();
        return storage.getSidedWrapper(direction);
    }

    final FluidInventory inventory;
    final List<FluidInventorySlotWrapper> backingList;
    final MarkDirtyParticipant markDirtyParticipant = new MarkDirtyParticipant();

    public FluidInventoryStorageImpl(FluidInventory inventory) {
        super(Collections.emptyList());
        this.inventory = inventory;
        this.backingList = new ArrayList<>();
    }

    @Override
    public List<SingleSlotStorage<FluidVariant>> getSlots() {
        return parts;
    }

    private void resizeSlotList() {
        int inventorySize = inventory.size();

        if (inventorySize != parts.size()) {
            while (backingList.size() < inventorySize) {
                backingList.add(new FluidInventorySlotWrapper(this, backingList.size()));
            }

            parts = Collections.unmodifiableList(backingList.subList(0, inventorySize));
        }
    }

    private FluidInventoryStorage getSidedWrapper(@Nullable Direction direction) {
        if (inventory instanceof SidedFluidInventory && direction != null) {
            return new SidedFluidInventoryStorageImpl(this, direction);
        } else {
            return this;
        }
    }

    @Override
    public String toString() {
        return "FluidInventoryStorage[" + FluidInventoryStorage.toString(inventory) + "]";
    }

    class MarkDirtyParticipant extends SnapshotParticipant<Boolean> {
        @Override
        protected Boolean createSnapshot() {
            return Boolean.TRUE;
        }

        @Override
        protected void readSnapshot(Boolean snapshot) {
        }

        @Override
        protected void onFinalCommit() {
            inventory.markDirty();
        }
    }
}
