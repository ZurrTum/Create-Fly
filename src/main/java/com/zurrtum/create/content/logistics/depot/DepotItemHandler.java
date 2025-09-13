package com.zurrtum.create.content.logistics.depot;

import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;
import com.zurrtum.create.infrastructure.items.ItemInventory;
import net.minecraft.item.ItemStack;

public class DepotItemHandler implements ItemInventory {
    private final DepotBehaviour behaviour;

    public DepotItemHandler(DepotBehaviour behaviour) {
        this.behaviour = behaviour;
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        if (slot != 0)
            return false;
        if (!behaviour.isItemValid(stack) || !behaviour.canAcceptItems.get()) {
            return false;
        }
        if (behaviour.canMergeItems()) {
            Integer max = behaviour.maxStackSize.get();
            if (max == 0) {
                return true;
            }
            return behaviour.getHeldItemStack().getCount() < max;
        } else {
            return behaviour.getHeldItemStack().isEmpty() && behaviour.isOutputEmpty();
        }
    }

    @Override
    public int size() {
        return 9;
    }

    @Override
    public ItemStack getStack(int slot) {
        return slot == 0 ? behaviour.getHeldItemStack() : behaviour.processingOutputBuffer.getStack(slot - 1);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (slot == 0) {
            if (stack.isEmpty()) {
                behaviour.removeHeldItem();
            } else {
                behaviour.setHeldItem(new TransportedItemStack(stack));
            }
        } else {
            behaviour.processingOutputBuffer.setStack(slot - 1, stack);
        }
    }

    @Override
    public void markDirty() {
        behaviour.blockEntity.notifyUpdate();
    }
}
