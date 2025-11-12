package com.zurrtum.create.content.fluids.drain;

import com.zurrtum.create.content.fluids.transfer.GenericItemEmptying;
import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;
import com.zurrtum.create.infrastructure.items.ItemInventory;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class ItemDrainItemHandler implements ItemInventory {
    private final ItemDrainBlockEntity blockEntity;
    private final Direction side;

    public ItemDrainItemHandler(ItemDrainBlockEntity be, Direction side) {
        this.blockEntity = be;
        this.side = side.getOpposite();
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        if (GenericItemEmptying.canItemBeEmptied(blockEntity.getLevel(), stack)) {
            return 1;
        } else {
            return stack.getMaxStackSize();
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return blockEntity.getHeldItemStack().isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot > 1) {
            return ItemStack.EMPTY;
        }
        return blockEntity.getHeldItemStack();
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot > 1) {
            return;
        }
        if (stack.isEmpty()) {
            blockEntity.heldItem = null;
        } else {
            TransportedItemStack heldItem = new TransportedItemStack(stack);
            heldItem.prevBeltPosition = 0;
            blockEntity.setHeldItem(heldItem, side);
        }
    }

    @Override
    public void setChanged() {
        blockEntity.notifyUpdate();
    }
}
