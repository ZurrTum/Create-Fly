package com.zurrtum.create.content.fluids.drain;

import com.zurrtum.create.content.fluids.transfer.GenericItemEmptying;
import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;
import com.zurrtum.create.infrastructure.items.ItemInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;

public class ItemDrainItemHandler implements ItemInventory {
    private final ItemDrainBlockEntity blockEntity;
    private final Direction side;

    public ItemDrainItemHandler(ItemDrainBlockEntity be, Direction side) {
        this.blockEntity = be;
        this.side = side.getOpposite();
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public int getMaxCount(ItemStack stack) {
        if (GenericItemEmptying.canItemBeEmptied(blockEntity.getWorld(), stack)) {
            return 1;
        } else {
            return stack.getMaxCount();
        }
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return blockEntity.getHeldItemStack().isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        if (slot > 1) {
            return ItemStack.EMPTY;
        }
        return blockEntity.getHeldItemStack();
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
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
    public void markDirty() {
        blockEntity.notifyUpdate();
    }
}
