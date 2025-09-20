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

    @Override
    default void create$setStack(int slot, ItemStack stack) {
        setStack(slot, stack);
    }

    @Override
    default int create$size() {
        return size();
    }
}
