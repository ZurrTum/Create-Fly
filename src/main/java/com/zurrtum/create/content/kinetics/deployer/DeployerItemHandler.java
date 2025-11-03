package com.zurrtum.create.content.kinetics.deployer;

import com.zurrtum.create.infrastructure.items.SidedItemInventory;
import com.zurrtum.create.infrastructure.transfer.SlotRangeCache;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class DeployerItemHandler implements SidedItemInventory {
    private final DeployerBlockEntity be;
    private final DeployerPlayer player;

    public DeployerItemHandler(DeployerBlockEntity be) {
        this.be = be;
        this.player = be.player;
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return SlotRangeCache.get(be.overflowItems.size() + 1);
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        if (slot != 0 || player == null) {
            return false;
        }
        if (be.filtering.getFilter().isEmpty()) {
            return true;
        }
        return be.filtering.test(stack);
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        if (slot != 0) {
            return true;
        }
        if (player == null) {
            return false;
        }
        if (be.filtering.getFilter().isEmpty()) {
            return true;
        }
        return !be.filtering.test(player.cast().getMainHandStack());
    }

    @Override
    public int size() {
        return 1 + be.overflowItems.size();
    }

    @Override
    public ItemStack getStack(int slot) {
        int size = be.overflowItems.size();
        if (slot > size) {
            return ItemStack.EMPTY;
        }
        if (slot == 0) {
            return player == null ? ItemStack.EMPTY : player.cast().getMainHandStack();
        }
        return be.overflowItems.get(slot - 1);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        int size = be.overflowItems.size();
        if (slot > size) {
            return;
        }
        if (slot == 0) {
            player.cast().setStackInHand(Hand.MAIN_HAND, stack);
        } else {
            be.overflowItems.set(slot - 1, stack);
        }
    }

    @Override
    public void markDirty() {
        be.overflowItems.removeIf(ItemStack::isEmpty);
        be.notifyUpdate();
    }
}
