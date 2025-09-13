package com.zurrtum.create.content.logistics.stockTicker;

import com.zurrtum.create.AllMenuTypes;
import com.zurrtum.create.foundation.gui.menu.MenuBase;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

import java.util.List;

public class StockKeeperRequestMenu extends MenuBase<StockTickerBlockEntity> {

    public boolean isAdmin;
    public boolean isLocked;

    public Object screenReference;

    public StockKeeperRequestMenu(int id, PlayerInventory inv, StockTickerBlockEntity contentHolder) {
        super(AllMenuTypes.STOCK_KEEPER_REQUEST, id, inv, contentHolder);
    }

    @Override
    protected void initAndReadInventory(StockTickerBlockEntity contentHolder) {
    }

    @Override
    public void updateSlotStacks(int pStateId, List<ItemStack> pItems, ItemStack pCarried) {
    }

    @Override
    protected void addSlots() {
        addPlayerSlots(-1000, 0);
    }

    @Override
    protected void saveData(StockTickerBlockEntity contentHolder) {
    }

    @Override
    public ItemStack quickMove(PlayerEntity pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

}
