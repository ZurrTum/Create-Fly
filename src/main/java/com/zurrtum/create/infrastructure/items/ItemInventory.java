package com.zurrtum.create.infrastructure.items;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface ItemInventory extends Container {
    @Override
    @SuppressWarnings("deprecation")
    default ItemStack removeItem(int slot, int amount) {
        if (slot >= getContainerSize() || amount <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = getItem(slot);
        int count = stack.getCount();
        if (count == 0) {
            return stack;
        }
        if (amount >= count) {
            setItem(slot, ItemStack.EMPTY);
            return onExtract(stack);
        }
        assert stack.item != null;
        ItemStack extract = new ItemStack(stack.item, amount, stack.components.copy());
        extract.setPopTime(stack.getPopTime());
        stack.setCount(count - amount);
        return onExtract(extract);
    }

    @Override
    default ItemStack removeItemNoUpdate(int slot) {
        if (slot >= getContainerSize()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = getItem(slot);
        if (stack.isEmpty()) {
            return stack;
        }
        setItem(slot, ItemStack.EMPTY);
        return onExtract(stack);
    }

    @Override
    default boolean stillValid(Player player) {
        return true;
    }

    @Override
    default boolean isEmpty() {
        for (int i = 0, size = getContainerSize(); i < size; i++) {
            if (!getItem(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    default void clearContent() {
        for (int i = 0, size = getContainerSize(); i < size; i++) {
            setItem(i, ItemStack.EMPTY);
        }
        setChanged();
    }

    @Override
    default void setChanged() {
    }
}
