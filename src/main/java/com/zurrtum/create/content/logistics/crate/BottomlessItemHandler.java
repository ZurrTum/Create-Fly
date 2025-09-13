package com.zurrtum.create.content.logistics.crate;

import com.zurrtum.create.infrastructure.items.ItemInventory;
import net.minecraft.item.ItemStack;

import java.util.function.Supplier;

public class BottomlessItemHandler implements ItemInventory {
    private final Supplier<ItemStack> suppliedItemStack;
    private ItemStack stack = ItemStack.EMPTY;

    public BottomlessItemHandler(Supplier<ItemStack> suppliedItemStack) {
        this.suppliedItemStack = suppliedItemStack;
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public ItemStack getStack(int slot) {
        if (slot == 0)
            return stack;
        return ItemStack.EMPTY;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
    }

    @Override
    public void markDirty() {
        ItemStack stack = suppliedItemStack.get();
        int max = stack.getMaxCount();
        if (stack == ItemStack.EMPTY || this.stack.isOf(stack.getItem())) {
            this.stack.setCount(max);
        } else {
            this.stack = stack.copyWithCount(max);
        }
    }
}
