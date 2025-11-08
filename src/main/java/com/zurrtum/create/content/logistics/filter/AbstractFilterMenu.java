package com.zurrtum.create.content.logistics.filter;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.foundation.gui.menu.HeldItemGhostItemMenu;
import com.zurrtum.create.foundation.gui.menu.MenuType;
import com.zurrtum.create.foundation.item.ItemHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

public abstract class AbstractFilterMenu extends HeldItemGhostItemMenu {
    protected AbstractFilterMenu(MenuType<ItemStack> type, int id, PlayerInventory inv, ItemStack contentHolder) {
        super(type, id, inv, contentHolder);
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
}
