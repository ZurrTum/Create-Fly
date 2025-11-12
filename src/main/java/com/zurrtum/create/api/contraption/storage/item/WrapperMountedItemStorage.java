package com.zurrtum.create.api.contraption.storage.item;

import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/**
 * Partial implementation of a MountedItemStorage that wraps an item handler.
 */
public abstract class WrapperMountedItemStorage<T extends Container> extends MountedItemStorage {
    protected T wrapped;

    protected WrapperMountedItemStorage(MountedItemStorageType<?> type) {
        super(type);
    }

    protected WrapperMountedItemStorage(MountedItemStorageType<?> type, T wrapped) {
        super(type);
        this.wrapped = wrapped;
    }

    @Override
    public int getContainerSize() {
        return wrapped.getContainerSize();
    }

    @Override
    public ItemStack getItem(int slot) {
        return wrapped.getItem(slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        wrapped.setItem(slot, stack);
    }

    @Override
    public int getMaxStackSize() {
        return wrapped.getMaxStackSize();
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return wrapped.getMaxStackSize(stack);
    }

    @Override
    public int insert(ItemStack stack) {
        return wrapped.insert(stack);
    }

    @Override
    public int extract(ItemStack stack) {
        return wrapped.extract(stack);
    }

    public static ItemStackHandler copyToItemStackHandler(Container handler) {
        int size = handler.getContainerSize();
        ItemStackHandler copy = new ItemStackHandler(size);
        for (int i = 0; i < size; i++) {
            copy.setItem(i, handler.getItem(i).copy());
        }
        return copy;
    }
}
