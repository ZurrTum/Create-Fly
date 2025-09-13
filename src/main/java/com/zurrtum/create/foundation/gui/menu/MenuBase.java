package com.zurrtum.create.foundation.gui.menu;

import com.zurrtum.create.foundation.utility.IInteractionChecker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public abstract class MenuBase<T> extends ScreenHandler {
    public PlayerEntity player;
    public PlayerInventory playerInventory;
    public T contentHolder;
    private final MenuType<T> type;

    protected MenuBase(MenuType<T> type, int id, PlayerInventory inv, T contentHolder) {
        super(null, id);
        this.type = type;
        init(inv, contentHolder);
    }

    public MenuType<T> getMenuType() {
        return type;
    }

    protected void init(PlayerInventory inv, T contentHolderIn) {
        player = inv.player;
        playerInventory = inv;
        contentHolder = contentHolderIn;
        initAndReadInventory(contentHolder);
        addSlots();
        sendContentUpdates();
    }

    protected abstract void initAndReadInventory(T contentHolder);

    protected abstract void addSlots();

    protected abstract void saveData(T contentHolder);

    protected void addPlayerSlots(int x, int y) {
        for (int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot)
            this.addSlot(new Slot(playerInventory, hotbarSlot, x + hotbarSlot * 18, y + 58));
        for (int row = 0; row < 3; ++row)
            for (int col = 0; col < 9; ++col)
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, x + col * 18, y + row * 18));
    }

    @Override
    public void onClosed(PlayerEntity playerIn) {
        super.onClosed(playerIn);
        saveData(contentHolder);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        if (contentHolder == null)
            return false;
        if (contentHolder instanceof IInteractionChecker)
            return ((IInteractionChecker) contentHolder).canPlayerUse(player);
        return true;
    }

}
