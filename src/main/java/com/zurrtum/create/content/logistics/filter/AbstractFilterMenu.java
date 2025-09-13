package com.zurrtum.create.content.logistics.filter;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.foundation.gui.menu.GhostItemMenu;
import com.zurrtum.create.foundation.gui.menu.MenuType;
import com.zurrtum.create.foundation.item.ItemHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public abstract class AbstractFilterMenu extends GhostItemMenu<ItemStack> {
    protected AbstractFilterMenu(MenuType<ItemStack> type, int id, PlayerInventory inv, ItemStack contentHolder) {
        super(type, id, inv, contentHolder);
    }

    @Override
    public void onSlotClick(int slotId, int dragType, SlotActionType clickTypeIn, PlayerEntity player) {
        if (slotId == playerInventory.getSelectedSlot() && clickTypeIn != SlotActionType.THROW)
            return;
        super.onSlotClick(slotId, dragType, clickTypeIn, player);
    }

    @Override
    protected boolean allowRepeats() {
        return false;
    }

    protected abstract int getPlayerInventoryXOffset();

    protected abstract int getPlayerInventoryYOffset();

    protected abstract void addFilterSlots();

    @Override
    protected void addSlots() {
        addPlayerSlots(getPlayerInventoryXOffset(), getPlayerInventoryYOffset());
        addFilterSlots();
    }

    @Override
    protected void saveData(ItemStack contentHolder) {
        if (!ghostInventory.isEmpty()) {
            contentHolder.set(AllDataComponents.FILTER_ITEMS, ItemHelper.containerContentsFromHandler(ghostInventory));
        } else {
            contentHolder.remove(AllDataComponents.FILTER_ITEMS);
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return playerInventory.getSelectedStack() == contentHolder;
    }

}
