package com.zurrtum.create.content.logistics.filter;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllMenuTypes;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class FilterMenu extends AbstractFilterMenu {

    public boolean respectNBT;
    public boolean blacklist;

    public FilterMenu(int id, PlayerInventory inv, ItemStack stack) {
        super(AllMenuTypes.FILTER, id, inv, stack);
    }

    @Override
    protected int getPlayerInventoryXOffset() {
        return 38;
    }

    @Override
    protected int getPlayerInventoryYOffset() {
        return 121;
    }

    @Override
    protected void addFilterSlots() {
        int x = 23;
        int y = 25;
        for (int row = 0; row < 2; ++row)
            for (int col = 0; col < 9; ++col)
                this.addSlot(new Slot(ghostInventory, col + row * 9, x + col * 18, y + row * 18));
    }

    @Override
    protected ItemStackHandler createGhostInventory() {
        return AllItems.FILTER.getFilterItemHandler(contentHolder);
    }

    @Override
    protected void initAndReadInventory(ItemStack filterItem) {
        super.initAndReadInventory(filterItem);
        respectNBT = filterItem.getOrDefault(AllDataComponents.FILTER_ITEMS_RESPECT_NBT, false);
        blacklist = filterItem.getOrDefault(AllDataComponents.FILTER_ITEMS_BLACKLIST, false);
    }

    @Override
    protected void saveData(ItemStack filterItem) {
        super.saveData(filterItem);
        filterItem.set(AllDataComponents.FILTER_ITEMS_RESPECT_NBT, respectNBT);
        filterItem.set(AllDataComponents.FILTER_ITEMS_BLACKLIST, blacklist);

        if (respectNBT || blacklist)
            return;
        if (!ghostInventory.isEmpty())
            return;
        filterItem.remove(AllDataComponents.FILTER_ITEMS_RESPECT_NBT);
        filterItem.remove(AllDataComponents.FILTER_ITEMS_BLACKLIST);
    }

}