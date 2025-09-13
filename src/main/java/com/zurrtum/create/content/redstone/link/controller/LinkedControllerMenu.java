package com.zurrtum.create.content.redstone.link.controller;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllMenuTypes;
import com.zurrtum.create.foundation.gui.menu.GhostItemMenu;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public class LinkedControllerMenu extends GhostItemMenu<ItemStack> {
    public LinkedControllerMenu(int id, PlayerInventory inv, ItemStack filterItem) {
        super(AllMenuTypes.LINKED_CONTROLLER, id, inv, filterItem);
    }

    @Override
    protected ItemStackHandler createGhostInventory() {
        return LinkedControllerItem.getFrequencyItems(contentHolder);
    }

    @Override
    protected void addSlots() {
        addPlayerSlots(8, 131);

        int x = 12;
        int y = 34;
        int slot = 0;

        for (int column = 0; column < 6; column++) {
            for (int row = 0; row < 2; ++row)
                addSlot(new Slot(ghostInventory, slot++, x, y + row * 18));
            x += 24;
            if (column == 3)
                x += 11;
        }
    }

    @Override
    protected void saveData(ItemStack contentHolder) {
        contentHolder.set(AllDataComponents.LINKED_CONTROLLER_ITEMS, ItemHelper.containerContentsFromHandler(ghostInventory));
    }

    @Override
    protected boolean allowRepeats() {
        return true;
    }

    @Override
    public void onSlotClick(int slotId, int dragType, SlotActionType clickTypeIn, PlayerEntity player) {
        if (slotId == playerInventory.getSelectedSlot() && clickTypeIn != SlotActionType.THROW)
            return;
        super.onSlotClick(slotId, dragType, clickTypeIn, player);
    }

    @Override
    public boolean canUse(PlayerEntity playerIn) {
        return playerInventory.getSelectedStack() == contentHolder;
    }

}
