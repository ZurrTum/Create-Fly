package com.zurrtum.create.infrastructure.transfer;

import com.zurrtum.create.infrastructure.fluids.FluidItemInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayDeque;
import java.util.Deque;

public class FluidItemInventoryWrapper extends FluidInventoryWrapper<Storage<FluidVariant>, StorageView<FluidVariant>> implements FluidItemInventory {
    private static final Deque<FluidItemInventoryWrapper> POOL = new ArrayDeque<>();
    private FluidItemContext context;
    private int size;

    private FluidItemInventoryWrapper(Storage<FluidVariant> storage) {
        super(storage);
    }

    public static FluidItemInventory of(Storage<FluidVariant> storage, FluidItemContext context) {
        FluidItemInventoryWrapper inventory = POOL.pollFirst();
        if (inventory == null) {
            inventory = new FluidItemInventoryWrapper(storage);
        } else {
            inventory.storage = storage;
            inventory.init();
        }
        inventory.context = context;
        return inventory;
    }

    @Override
    public void close() {
        context.close();
        storage = null;
        context = null;
        POOL.addLast(this);
    }

    @Override
    public ItemStack getContainer() {
        return context.getStack();
    }

    @Override
    public boolean isEmpty() {
        return context.getStack().isEmpty();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int insert(FluidStack stack) {
        if (!storage.supportsInsertion()) {
            return 0;
        }
        return super.insert(stack);
    }

    @Override
    public int insert(FluidStack stack, int maxAmount) {
        if (!storage.supportsInsertion()) {
            return 0;
        }
        return super.insert(stack, maxAmount);
    }

    @Override
    public boolean preciseInsert(FluidStack stack) {
        if (!storage.supportsInsertion()) {
            return false;
        }
        return super.preciseInsert(stack);
    }

    @Override
    public int extract(FluidStack stack) {
        if (!storage.supportsExtraction()) {
            return 0;
        }
        return super.extract(stack);
    }

    @Override
    public int extract(FluidStack stack, int maxAmount) {
        if (!storage.supportsExtraction()) {
            return 0;
        }
        return super.extract(stack, maxAmount);
    }

    @Override
    public FluidStack extractAny(int maxAmount) {
        if (!storage.supportsExtraction()) {
            return FluidStack.EMPTY;
        }
        return super.extractAny(maxAmount);
    }

    @Override
    public boolean preciseExtract(FluidStack stack) {
        if (!storage.supportsExtraction()) {
            return false;
        }
        return super.preciseExtract(stack);
    }

    @Override
    public int count(FluidStack stack) {
        if (!storage.supportsExtraction()) {
            return 0;
        }
        return super.count(stack);
    }

    @Override
    public int count(FluidStack stack, int maxAmount) {
        if (!storage.supportsExtraction()) {
            return 0;
        }
        return super.count(stack, maxAmount);
    }

    @Override
    public int countSpace(FluidStack stack) {
        if (!storage.supportsInsertion()) {
            return 0;
        }
        return super.countSpace(stack);
    }

    @Override
    public int countSpace(FluidStack stack, int maxAmount) {
        if (!storage.supportsInsertion()) {
            return 0;
        }
        return super.countSpace(stack, maxAmount);
    }

    @Override
    public FluidStack removeStack(int slot) {
        if (!storage.supportsExtraction()) {
            return FluidStack.EMPTY;
        }
        return super.removeStack(slot);
    }

    @Override
    public FluidStack removeStack(int slot, int amount) {
        if (!storage.supportsExtraction()) {
            return FluidStack.EMPTY;
        }
        return super.removeStack(slot, amount);
    }

    @Override
    protected void insert(StorageView<FluidVariant> view, FluidVariant variant, int amount, Transaction transaction) {
        storage.insert(variant, amount, transaction);
    }

    @Override
    protected StorageView<FluidVariant> getSlot(int slot) {
        if (storage instanceof SlottedStorage<FluidVariant> slottedStorage) {
            return slottedStorage.getSlot(slot);
        }
        int current = 0;
        for (StorageView<FluidVariant> view : storage) {
            if (current == slot) {
                return view;
            }
            current++;
        }
        return null;
    }

    @Override
    protected void init() {
        if (storage instanceof SlottedStorage<FluidVariant> slottedStorage) {
            size = slottedStorage.getSlotCount();
            super.init();
        } else {
            int count = 0;
            int max = 0;
            for (StorageView<FluidVariant> view : storage) {
                long capacity = view.getCapacity();
                if (max < capacity) {
                    max = (int) capacity;
                }
                count++;
            }
            size = count;
            capacity = max;
        }
    }
}
