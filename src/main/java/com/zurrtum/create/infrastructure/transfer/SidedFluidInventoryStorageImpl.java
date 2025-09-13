package com.zurrtum.create.infrastructure.transfer;

import com.zurrtum.create.infrastructure.fluids.SidedFluidInventory;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.minecraft.util.math.Direction;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SidedFluidInventoryStorageImpl extends CombinedStorage<FluidVariant, SingleSlotStorage<FluidVariant>> implements FluidInventoryStorage {
    private final FluidInventoryStorageImpl backingStorage;

    SidedFluidInventoryStorageImpl(FluidInventoryStorageImpl storage, Direction direction) {
        super(Collections.unmodifiableList(createWrapperList(storage, direction)));
        this.backingStorage = storage;
    }

    @Override
    public List<SingleSlotStorage<FluidVariant>> getSlots() {
        return parts;
    }

    private static List<SingleSlotStorage<FluidVariant>> createWrapperList(FluidInventoryStorageImpl storage, Direction direction) {
        SidedFluidInventory inventory = (SidedFluidInventory) storage.inventory;
        int[] availableSlots = inventory.getAvailableSlots(direction);
        SidedFluidInventorySlotWrapper[] slots = new SidedFluidInventorySlotWrapper[availableSlots.length];

        for (int i = 0; i < availableSlots.length; ++i) {
            slots[i] = new SidedFluidInventorySlotWrapper(storage.backingList.get(availableSlots[i]), inventory, direction);
        }

        return Arrays.asList(slots);
    }

    @Override
    public String toString() {
        return backingStorage.toString();
    }
}
