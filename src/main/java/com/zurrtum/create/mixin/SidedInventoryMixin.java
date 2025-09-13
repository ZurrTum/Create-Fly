package com.zurrtum.create.mixin;

import com.zurrtum.create.infrastructure.items.SidedItemInventory;
import net.minecraft.inventory.SidedInventory;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SidedInventory.class)
public interface SidedInventoryMixin extends SidedItemInventory {
}
