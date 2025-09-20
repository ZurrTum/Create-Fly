package com.zurrtum.create.mixin;

import com.zurrtum.create.infrastructure.items.BaseInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Inventory.class)
public interface InventoryMixin extends BaseInventory {
    @Shadow
    void setStack(int slot, ItemStack stack);

    @Shadow
    int size();

    @Shadow
    int getMaxCount(ItemStack stack);

    @Shadow
    ItemStack getStack(int slot);

    @Shadow
    boolean isValid(int slot, ItemStack stack);

    @Shadow
    void markDirty();

    @Override
    default void create$setStack(int slot, ItemStack stack) {
        setStack(slot, stack);
    }

    @Override
    default int create$size() {
        return size();
    }

    @Override
    default int create$getMaxCount(ItemStack stack) {
        return getMaxCount(stack);
    }

    @Override
    default ItemStack create$getStack(int slot) {
        return getStack(slot);
    }

    @Override
    default boolean create$isValid(int slot, ItemStack stack) {
        return isValid(slot, stack);
    }

    @Override
    default void create$markDirty() {
        markDirty();
    }
}
