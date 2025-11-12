package com.zurrtum.create.content.logistics.chute;

import com.zurrtum.create.infrastructure.items.ItemInventory;
import net.minecraft.world.item.ItemStack;

public class ChuteItemHandler implements ItemInventory {
    private final ChuteBlockEntity blockEntity;

    public ChuteItemHandler(ChuteBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return blockEntity.canAcceptItem(stack);
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot != 0) {
            return ItemStack.EMPTY;
        }
        return blockEntity.item;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot != 0) {
            return;
        }
        blockEntity.setItem(stack);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return Math.min(stack.getMaxStackSize(), 64);
    }
}
