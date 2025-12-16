package com.zurrtum.create.foundation.gui.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * A {@link GhostItemMenu} that is linked to the item in a player's main hand. Prevents its owner item from being manipulated.
 */
public abstract class HeldItemGhostItemMenu extends GhostItemMenu<ItemStack> {
    protected HeldItemGhostItemMenu(MenuType<ItemStack> type, int id, Inventory inv, ItemStack contentHolder) {
        super(type, id, inv, contentHolder);
    }

    @Override
    public void clicked(int slotId, int dragType, ContainerInput clickTypeIn, Player player) {
        if (slotId == playerInventory.getSelectedSlot() && clickTypeIn != ContainerInput.THROW)
            return;
        super.clicked(slotId, dragType, clickTypeIn, player);
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        // prevent pick-all from taking the owner item out of its slot
        return super.canTakeItemForPickAll(stack, slot) && !this.isInSlot(slot.index);
    }

    @Override
    public boolean stillValid(Player player) {
        return playerInventory.getSelectedItem() == contentHolder;
    }

    protected boolean isInSlot(int index) {
        // Inventory has the hotbar as 0-8, but menus put the hotbar at 27-35
        return index >= 27 && index - 27 == playerInventory.getSelectedSlot();
    }
}
