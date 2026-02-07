package com.zurrtum.create.content.logistics.chute;

import com.zurrtum.create.infrastructure.items.ItemInventory;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;

public class ChuteItemHandler implements ItemInventory {
    private final ChuteBlockEntity blockEntity;

    public ChuteItemHandler(ChuteBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return blockEntity.canAcceptItem(stack);
    }

    @Override
    public ItemStack getStack(int slot) {
        if (slot != 0) {
            return ItemStack.EMPTY;
        }
        return blockEntity.item;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (slot != 0) {
            return;
        }
        blockEntity.setItem(stack);
    }

    @Override
    public int getMaxCount(ItemStack stack) {
        return stack.getOrDefault(DataComponentTypes.MAX_STACK_SIZE, 64);
    }
}
