package com.zurrtum.create.foundation.gui.menu;

import com.zurrtum.create.foundation.utility.IInteractionChecker;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

public abstract class MenuBase<T> extends AbstractContainerMenu {
    public Player player;
    public Inventory playerInventory;
    public T contentHolder;
    private final MenuType<T> type;

    protected MenuBase(MenuType<T> type, int id, Inventory inv, T contentHolder) {
        super(null, id);
        this.type = type;
        init(inv, contentHolder);
    }

    public MenuType<T> getMenuType() {
        return type;
    }

    protected void init(Inventory inv, T contentHolderIn) {
        player = inv.player;
        playerInventory = inv;
        contentHolder = contentHolderIn;
        initAndReadInventory(contentHolder);
        addSlots();
        broadcastChanges();
    }

    protected abstract void initAndReadInventory(T contentHolder);

    protected abstract void addSlots();

    protected abstract void saveData(T contentHolder);

    protected void addPlayerSlots(int x, int y) {
        for (int row = 0; row < 3; ++row)
            for (int col = 0; col < 9; ++col)
                this.addSlot(this.createPlayerSlot(playerInventory, col + row * 9 + 9, x + col * 18, y + row * 18));
        for (int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot)
            this.addSlot(this.createPlayerSlot(playerInventory, hotbarSlot, x + hotbarSlot * 18, y + 58));
    }

    protected Slot createPlayerSlot(Inventory inventory, int index, int x, int y) {
        return new Slot(inventory, index, x, y);
    }

    @Override
    public void removed(Player playerIn) {
        super.removed(playerIn);
        saveData(contentHolder);
    }

    @Override
    public boolean stillValid(Player player) {
        if (contentHolder == null)
            return false;
        if (contentHolder instanceof IInteractionChecker)
            return ((IInteractionChecker) contentHolder).canPlayerUse(player);
        return true;
    }

}
