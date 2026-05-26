package com.zurrtum.create.content.logistics.crate;

import com.zurrtum.create.infrastructure.items.ItemInventory;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

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
        if (slot == 0) {
            return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot == 0 && stack == ItemStack.EMPTY) {
            stack = suppliedItemStack.get();
            if (stack != ItemStack.EMPTY) {
                this.stack = stack.copy();
            }
        }
    }

    @Override
    public void setChanged() {
        ItemStack stack = suppliedItemStack.get();
        if (stack == ItemStack.EMPTY) {
            this.stack = ItemStack.EMPTY;
        } else if (ItemStack.isSameItemSameComponents(this.stack, stack)) {
            this.stack.setCount(stack.getMaxStackSize());
        } else {
            this.stack = stack.copyWithCount(stack.getMaxStackSize());
        }
    }
}
