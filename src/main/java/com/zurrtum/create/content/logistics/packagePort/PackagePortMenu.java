package com.zurrtum.create.content.logistics.packagePort;

import com.zurrtum.create.AllMenuTypes;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.animatedContainer.AnimatedContainerBehaviour;
import com.zurrtum.create.foundation.gui.menu.MenuBase;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class PackagePortMenu extends MenuBase<PackagePortBlockEntity> {
    public PackagePortMenu(int id, PlayerInventory inv, PackagePortBlockEntity be) {
        super(AllMenuTypes.PACKAGE_PORT, id, inv, be);
        BlockEntityBehaviour.get(be, AnimatedContainerBehaviour.TYPE).startOpen(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        Slot clickedSlot = getSlot(index);
        if (!clickedSlot.hasStack())
            return ItemStack.EMPTY;

        ItemStack stack = clickedSlot.getStack();
        int size = contentHolder.inventory.size();
        boolean success;
        if (index < size) {
            success = !insertItem(stack, size, slots.size(), false);
        } else
            success = !insertItem(stack, 0, size, false);

        return success ? ItemStack.EMPTY : stack;
    }

    @Override
    protected void initAndReadInventory(PackagePortBlockEntity contentHolder) {
    }

    @Override
    protected void addSlots() {
        Inventory inventory = contentHolder.inventory;
        int x = 27;
        int y = 9;

        for (int row = 0; row < 2; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new Slot(inventory, row * 9 + col, x + col * 18, y + row * 18));

        addPlayerSlots(38, 108);
    }

    @Override
    protected void saveData(PackagePortBlockEntity contentHolder) {
    }

    @Override
    public void onClosed(PlayerEntity playerIn) {
        super.onClosed(playerIn);
        if (!playerIn.getEntityWorld().isClient())
            BlockEntityBehaviour.get(contentHolder, AnimatedContainerBehaviour.TYPE).stopOpen(playerIn);
    }

}
