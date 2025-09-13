package com.zurrtum.create.content.logistics.packager;

import com.zurrtum.create.api.packager.InventoryIdentifier;
import net.minecraft.inventory.Inventory;
import org.jetbrains.annotations.Nullable;

/**
 * An item inventory, possibly with an associated InventoryIdentifier.
 */
public record IdentifiedInventory(@Nullable InventoryIdentifier identifier, Inventory handler) {
}
