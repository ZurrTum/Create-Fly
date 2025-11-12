package com.zurrtum.create.content.logistics.packager;

import com.zurrtum.create.content.logistics.box.PackageItem;
import com.zurrtum.create.infrastructure.items.SidedItemInventory;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class PackagerItemHandler implements SidedItemInventory {
    private final int[] SLOTS = {0};
    private final PackagerBlockEntity blockEntity;

    public PackagerItemHandler(PackagerBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
        return blockEntity.heldBox.isEmpty() && blockEntity.queuedExitingPackages.isEmpty();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return PackageItem.isPackage(stack) && blockEntity.unwrapBox(stack, true);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return blockEntity.animationTicks == 0;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    public ItemStack getStack() {
        return blockEntity.heldBox;
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot != 0) {
            return ItemStack.EMPTY;
        }
        return blockEntity.heldBox;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot != 0) {
            return;
        }
        if (stack.isEmpty()) {
            blockEntity.heldBox = stack;
        } else {
            blockEntity.unwrapBox(stack, false);
            blockEntity.triggerStockCheck();
        }
    }

    @Override
    public void setChanged() {
        blockEntity.notifyUpdate();
    }
}
