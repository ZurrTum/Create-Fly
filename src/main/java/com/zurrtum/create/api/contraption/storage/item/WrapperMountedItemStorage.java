package com.zurrtum.create.api.contraption.storage.item;

import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

/**
 * Partial implementation of a MountedItemStorage that wraps an item handler.
 */
public abstract class WrapperMountedItemStorage<T extends Inventory> extends MountedItemStorage {
    protected T wrapped;

    protected WrapperMountedItemStorage(MountedItemStorageType<?> type) {
        super(type);
    }

    protected WrapperMountedItemStorage(MountedItemStorageType<?> type, T wrapped) {
        super(type);
        this.wrapped = wrapped;
    }

    @Override
    public int size() {
        return wrapped.size();
    }

    @Override
    public ItemStack getStack(int slot) {
        return wrapped.getStack(slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        wrapped.setStack(slot, stack);
    }

    @Override
    public int getMaxCountPerStack() {
        return wrapped.getMaxCountPerStack();
    }

    @Override
    public int getMaxCount(ItemStack stack) {
        return wrapped.getMaxCount(stack);
    }

    @Override
    public int insert(ItemStack stack) {
        return wrapped.insert(stack);
    }

    @Override
    public int extract(ItemStack stack) {
        return wrapped.extract(stack);
    }

    public static ItemStackHandler copyToItemStackHandler(Inventory handler) {
        int size = handler.size();
        ItemStackHandler copy = new ItemStackHandler(size);
        for (int i = 0; i < size; i++) {
            copy.setStack(i, handler.getStack(i).copy());
        }
        return copy;
    }
}
