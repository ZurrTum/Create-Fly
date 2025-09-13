package com.zurrtum.create.content.logistics.tunnel;

import com.zurrtum.create.infrastructure.items.ItemInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public class BrassTunnelItemHandler implements ItemInventory {
    private final BrassTunnelBlockEntity blockEntity;

    public BrassTunnelItemHandler(BrassTunnelBlockEntity be) {
        this.blockEntity = be;
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        if (blockEntity.hasDistributionBehaviour()) {
            return blockEntity.canTakeItems();
        }
        Inventory inventory = blockEntity.getBeltCapability();
        if (inventory == null) {
            return false;
        }
        return inventory.isValid(slot, stack);
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public int getMaxCount(ItemStack stack) {
        return blockEntity.stackToDistribute.isEmpty() ? 64 : blockEntity.stackToDistribute.getMaxCount();
    }

    @Override
    public ItemStack getStack(int slot) {
        if (slot != 0) {
            return ItemStack.EMPTY;
        }
        if (blockEntity.hasDistributionBehaviour()) {
            return blockEntity.stackToDistribute;
        } else {
            Inventory inventory = blockEntity.getBeltCapability();
            if (inventory == null) {
                return ItemStack.EMPTY;
            }
            return inventory.getStack(0);
        }
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (slot != 0) {
            return;
        }
        if (blockEntity.hasDistributionBehaviour()) {
            blockEntity.setStackToDistribute(stack, null);
        } else {
            Inventory inventory = blockEntity.getBeltCapability();
            if (inventory == null) {
                return;
            }
            inventory.setStack(0, stack);
        }
    }
}
