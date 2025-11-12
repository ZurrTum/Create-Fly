package com.zurrtum.create.content.logistics.tunnel;

import com.zurrtum.create.infrastructure.items.ItemInventory;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class BrassTunnelItemHandler implements ItemInventory {
    private final BrassTunnelBlockEntity blockEntity;

    public BrassTunnelItemHandler(BrassTunnelBlockEntity be) {
        this.blockEntity = be;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (blockEntity.hasDistributionBehaviour()) {
            return blockEntity.canTakeItems();
        }
        Container inventory = blockEntity.getBeltCapability();
        if (inventory == null) {
            return false;
        }
        return inventory.canPlaceItem(slot, stack);
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return blockEntity.stackToDistribute.isEmpty() ? 64 : blockEntity.stackToDistribute.getMaxStackSize();
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot != 0) {
            return ItemStack.EMPTY;
        }
        if (blockEntity.hasDistributionBehaviour()) {
            return blockEntity.stackToDistribute;
        } else {
            Container inventory = blockEntity.getBeltCapability();
            if (inventory == null) {
                return ItemStack.EMPTY;
            }
            return inventory.getItem(0);
        }
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot != 0) {
            return;
        }
        if (blockEntity.hasDistributionBehaviour()) {
            blockEntity.setStackToDistribute(stack, null);
        } else {
            Container inventory = blockEntity.getBeltCapability();
            if (inventory == null) {
                return;
            }
            inventory.setItem(0, stack);
        }
    }
}
