package com.zurrtum.create.mixin;

import com.zurrtum.create.infrastructure.items.SidedItemInventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SidedInventory.class)
public interface SidedInventoryMixin extends SidedItemInventory {
    @Shadow
    int[] getAvailableSlots(Direction side);

    @Shadow
    boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir);

    @Shadow
    boolean canExtract(int slot, ItemStack stack, Direction dir);

    @Override
    default int[] create$getAvailableSlots(Direction side) {
        return getAvailableSlots(side);
    }

    @Override
    default boolean create$canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return canInsert(slot, stack, dir);
    }

    @Override
    default boolean create$canExtract(int slot, ItemStack stack, Direction dir) {
        return canExtract(slot, stack, dir);
    }
}
