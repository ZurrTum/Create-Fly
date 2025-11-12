package com.zurrtum.create.content.trains.schedule;

import com.zurrtum.create.AllMenuTypes;
import com.zurrtum.create.foundation.gui.menu.HeldItemGhostItemMenu;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ScheduleMenu extends HeldItemGhostItemMenu {

    public boolean slotsActive = true;
    public int targetSlotsActive = 1;

    static final int slots = 2;

    public ScheduleMenu(int id, Inventory inv, ItemStack contentHolder) {
        super(AllMenuTypes.SCHEDULE, id, inv, contentHolder);
    }

    @Override
    protected ItemStackHandler createGhostInventory() {
        return new ItemStackHandler(slots);
    }

    @Override
    protected boolean allowRepeats() {
        return true;
    }

    @Override
    protected void addSlots() {
        addPlayerSlots(46, 140);
        for (int i = 0; i < slots; i++)
            addSlot(new InactiveItemHandlerSlot(ghostInventory, i, i, 54 + 20 * i, 88));
    }

    @Override
    protected void addPlayerSlots(int x, int y) {
        for (int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot)
            this.addSlot(new InactiveSlot(playerInventory, hotbarSlot, x + hotbarSlot * 18, y + 58));
        for (int row = 0; row < 3; ++row)
            for (int col = 0; col < 9; ++col)
                this.addSlot(new InactiveSlot(playerInventory, col + row * 9 + 9, x + col * 18, y + row * 18));
    }

    @Override
    protected void saveData(ItemStack contentHolder) {
    }

    class InactiveSlot extends Slot {

        public InactiveSlot(Container pContainer, int pIndex, int pX, int pY) {
            super(pContainer, pIndex, pX, pY);
        }

        @Override
        public boolean isActive() {
            return slotsActive;
        }

    }

    class InactiveItemHandlerSlot extends Slot {
        private final int targetIndex;

        public InactiveItemHandlerSlot(Container itemHandler, int targetIndex, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
            this.targetIndex = targetIndex;
        }

        @Override
        public boolean isActive() {
            return slotsActive && targetIndex < targetSlotsActive;
        }
    }

}
