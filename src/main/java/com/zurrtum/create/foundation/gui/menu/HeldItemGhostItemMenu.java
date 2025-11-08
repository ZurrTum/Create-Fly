package com.zurrtum.create.foundation.gui.menu;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

/**
 * A {@link GhostItemMenu} that is linked to the item in a player's main hand. Prevents its owner item from being manipulated.
 */
public abstract class HeldItemGhostItemMenu extends GhostItemMenu<ItemStack> {
    protected HeldItemGhostItemMenu(MenuType<ItemStack> type, int id, PlayerInventory inv, ItemStack contentHolder) {
        super(type, id, inv, contentHolder);
    }

    @Override
    public void onSlotClick(int slotId, int dragType, SlotActionType clickTypeIn, PlayerEntity player) {
        if (slotId == playerInventory.getSelectedSlot() && clickTypeIn != SlotActionType.THROW)
            return;
        super.onSlotClick(slotId, dragType, clickTypeIn, player);
    }

    @Override
    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
        // prevent pick-all from taking the owner item out of its slot
        return super.canInsertIntoSlot(stack, slot) && !this.isInSlot(slot.id);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return playerInventory.getSelectedStack() == contentHolder;
    }

    protected boolean isInSlot(int index) {
        // Inventory has the hotbar as 0-8, but menus put the hotbar at 27-35
        return index >= 27 && index - 27 == playerInventory.getSelectedSlot();
    }
}
