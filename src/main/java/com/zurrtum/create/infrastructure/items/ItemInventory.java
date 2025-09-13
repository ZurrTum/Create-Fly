package com.zurrtum.create.infrastructure.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public interface ItemInventory extends Inventory {
    @Override
    @SuppressWarnings("deprecation")
    default ItemStack removeStack(int slot, int amount) {
        if (slot >= size() || amount <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = getStack(slot);
        int count = stack.getCount();
        if (count == 0) {
            return stack;
        }
        if (amount >= count) {
            setStack(slot, ItemStack.EMPTY);
            return onExtract(stack);
        }
        assert stack.item != null;
        ItemStack extract = new ItemStack(stack.item, amount, stack.components.copy());
        extract.setBobbingAnimationTime(stack.getBobbingAnimationTime());
        stack.setCount(count - amount);
        return onExtract(extract);
    }

    @Override
    default ItemStack removeStack(int slot) {
        if (slot >= size()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = getStack(slot);
        if (stack.isEmpty()) {
            return stack;
        }
        setStack(slot, ItemStack.EMPTY);
        return onExtract(stack);
    }

    @Override
    default boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    default boolean isEmpty() {
        for (int i = 0, size = size(); i < size; i++) {
            if (!getStack(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    default void markDirty() {
    }
}
