package com.zurrtum.create.mixin;

import com.zurrtum.create.infrastructure.items.BaseSidedInventory;
import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(WorldlyContainer.class)
public interface SidedInventoryMixin extends BaseSidedInventory {
    @Shadow
    int[] getSlotsForFace(Direction side);

    @Shadow
    boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir);

    @Shadow
    boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir);

    @Override
    default int[] create$getAvailableSlots(Direction side) {
        return getSlotsForFace(side);
    }

    @Override
    default boolean create$canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return canPlaceItemThroughFace(slot, stack, dir);
    }

    @Override
    default boolean create$canExtract(int slot, ItemStack stack, Direction dir) {
        return canTakeItemThroughFace(slot, stack, dir);
    }
}
