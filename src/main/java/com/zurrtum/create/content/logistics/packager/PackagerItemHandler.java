package com.zurrtum.create.content.logistics.packager;

import com.zurrtum.create.content.logistics.box.PackageItem;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class PackagerItemHandler implements SidedInventory {
    private final int[] SLOTS = {0};
    private final PackagerBlockEntity blockEntity;

    public PackagerItemHandler(PackagerBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return SLOTS;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return blockEntity.heldBox.isEmpty() && blockEntity.queuedExitingPackages.isEmpty();
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return PackageItem.isPackage(stack) && blockEntity.unwrapBox(stack, true);
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return blockEntity.animationTicks == 0;
    }

    @Override
    public int size() {
        return 1;
    }

    public ItemStack getStack() {
        return blockEntity.heldBox;
    }

    @Override
    public ItemStack getStack(int slot) {
        if (slot != 0) {
            return ItemStack.EMPTY;
        }
        return blockEntity.heldBox;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
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
    public void markDirty() {
        blockEntity.notifyUpdate();
    }
}
