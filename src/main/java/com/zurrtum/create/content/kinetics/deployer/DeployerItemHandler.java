package com.zurrtum.create.content.kinetics.deployer;

import com.zurrtum.create.infrastructure.items.SidedItemInventory;
import com.zurrtum.create.infrastructure.transfer.SlotRangeCache;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class DeployerItemHandler implements SidedItemInventory {
    private final DeployerBlockEntity be;
    private final DeployerPlayer player;

    public DeployerItemHandler(DeployerBlockEntity be) {
        this.be = be;
        this.player = be.player;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return SlotRangeCache.get(be.overflowItems.size() + 1);
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
        if (slot != 0 || player == null) {
            return false;
        }
        if (be.filtering.getFilter().isEmpty()) {
            return true;
        }
        return be.filtering.test(stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        if (slot != 0) {
            return true;
        }
        if (player == null) {
            return false;
        }
        if (be.filtering.getFilter().isEmpty()) {
            return true;
        }
        return !be.filtering.test(player.cast().getMainHandItem());
    }

    @Override
    public int getContainerSize() {
        return 1 + be.overflowItems.size();
    }

    @Override
    public ItemStack getItem(int slot) {
        int size = be.overflowItems.size();
        if (slot > size) {
            return ItemStack.EMPTY;
        }
        if (slot == 0) {
            return player == null ? ItemStack.EMPTY : player.cast().getMainHandItem();
        }
        return be.overflowItems.get(slot - 1);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        int size = be.overflowItems.size();
        if (slot > size) {
            return;
        }
        if (slot == 0) {
            player.cast().setItemInHand(InteractionHand.MAIN_HAND, stack);
        } else {
            be.overflowItems.set(slot - 1, stack);
        }
    }

    @Override
    public void setChanged() {
        be.overflowItems.removeIf(ItemStack::isEmpty);
        be.notifyUpdate();
    }
}
