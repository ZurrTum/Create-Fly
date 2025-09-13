package com.zurrtum.create.content.logistics.redstoneRequester;

import com.zurrtum.create.AllMenuTypes;
import com.zurrtum.create.content.logistics.BigItemStack;
import com.zurrtum.create.content.logistics.stockTicker.PackageOrder;
import com.zurrtum.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import com.zurrtum.create.foundation.gui.menu.GhostItemMenu;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

import java.util.ArrayList;
import java.util.List;

public class RedstoneRequesterMenu extends GhostItemMenu<RedstoneRequesterBlockEntity> {

    public RedstoneRequesterMenu(int id, PlayerInventory inv, RedstoneRequesterBlockEntity contentHolder) {
        super(AllMenuTypes.REDSTONE_REQUESTER, id, inv, contentHolder);
    }

    @Override
    protected ItemStackHandler createGhostInventory() {
        ItemStackHandler inventory = new ItemStackHandler(9);
        List<BigItemStack> stacks = contentHolder.encodedRequest.stacks();
        for (int i = 0; i < stacks.size(); i++)
            inventory.setStack(i, stacks.get(i).stack.copyWithCount(1));
        return inventory;
    }

    @Override
    protected boolean allowRepeats() {
        return true;
    }

    @Override
    protected void addSlots() {
        int playerX = 5;
        int playerY = 142;
        int slotX = 27;
        int slotY = 28;

        addPlayerSlots(playerX, playerY);
        for (int i = 0; i < 9; i++)
            addSlot(new Slot(ghostInventory, i, slotX + 20 * i, slotY));
    }

    @Override
    protected void saveData(RedstoneRequesterBlockEntity contentHolder) {
        List<BigItemStack> stacks = contentHolder.encodedRequest.stacks();
        ArrayList<BigItemStack> list = new ArrayList<>();
        for (int i = 0, size = ghostInventory.size(), listSize = stacks.size(); i < size; i++) {
            ItemStack stackInSlot = ghostInventory.getStack(i);
            if (stackInSlot.isEmpty())
                continue;
            list.add(new BigItemStack(stackInSlot.copyWithCount(1), i < listSize ? stacks.get(i).count : 1));
        }

        PackageOrderWithCrafts newRequest = new PackageOrderWithCrafts(new PackageOrder(list), contentHolder.encodedRequest.orderedCrafts());
        if (!newRequest.orderedStacksMatchOrderedRecipes())
            newRequest = PackageOrderWithCrafts.simple(newRequest.stacks());
        contentHolder.encodedRequest = newRequest;
        contentHolder.sendData();
    }
}
