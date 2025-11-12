package com.zurrtum.create.content.logistics.stockTicker;

import com.zurrtum.create.AllMenuTypes;
import com.zurrtum.create.foundation.gui.menu.MenuBase;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class StockKeeperRequestMenu extends MenuBase<StockTickerBlockEntity> {

    public boolean isAdmin;
    public boolean isLocked;

    public Object screenReference;

    public StockKeeperRequestMenu(int id, Inventory inv, StockTickerBlockEntity contentHolder) {
        super(AllMenuTypes.STOCK_KEEPER_REQUEST, id, inv, contentHolder);
    }

    @Override
    protected void initAndReadInventory(StockTickerBlockEntity contentHolder) {
    }

    @Override
    public void initializeContents(int pStateId, List<ItemStack> pItems, ItemStack pCarried) {
    }

    @Override
    protected void addSlots() {
        addPlayerSlots(-1000, 0);
    }

    @Override
    protected void saveData(StockTickerBlockEntity contentHolder) {
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

}
