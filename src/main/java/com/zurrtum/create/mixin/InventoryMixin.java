package com.zurrtum.create.mixin;

import com.zurrtum.create.infrastructure.items.BaseInventory;
import net.minecraft.inventory.Inventory;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Inventory.class)
public interface InventoryMixin extends BaseInventory {
}
