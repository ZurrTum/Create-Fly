package com.zurrtum.create.content.logistics.crate;

import com.zurrtum.create.infrastructure.items.ItemInventory;

import java.util.function.Supplier;

import net.minecraft.world.item.ItemStack;

public class BottomlessItemHandler implements ItemInventory {
    private final Supplier<ItemStack> suppliedItemStack;
    private ItemStack stack = ItemStack.EMPTY;

    public BottomlessItemHandler(Supplier<ItemStack> suppliedItemStack) {
        this.suppliedItemStack = suppliedItemStack;
    }

    @Override
    public int getContainerSize() {
        return 2;
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot == 0)
            return stack;
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
    }

    @Override
    public void setChanged() {
        ItemStack stack = suppliedItemStack.get();
        int max = stack.getMaxStackSize();
        if (stack == ItemStack.EMPTY || this.stack.is(stack.getItem())) {
            this.stack.setCount(max);
        } else {
            this.stack = stack.copyWithCount(max);
        }
    }
}
