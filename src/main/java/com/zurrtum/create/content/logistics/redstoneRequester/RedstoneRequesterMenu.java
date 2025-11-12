package com.zurrtum.create.content.logistics.redstoneRequester;

import com.zurrtum.create.AllMenuTypes;
import com.zurrtum.create.content.logistics.BigItemStack;
import com.zurrtum.create.content.logistics.stockTicker.PackageOrder;
import com.zurrtum.create.foundation.gui.menu.GhostItemMenu;
import com.zurrtum.create.infrastructure.component.PackageOrderWithCrafts;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class RedstoneRequesterMenu extends GhostItemMenu<RedstoneRequesterBlockEntity> {

    public RedstoneRequesterMenu(int id, Inventory inv, RedstoneRequesterBlockEntity contentHolder) {
        super(AllMenuTypes.REDSTONE_REQUESTER, id, inv, contentHolder);
    }

    @Override
    protected ItemStackHandler createGhostInventory() {
        ItemStackHandler inventory = new ItemStackHandler(9);
        List<BigItemStack> stacks = contentHolder.encodedRequest.stacks();
        for (int i = 0; i < stacks.size(); i++)
            inventory.setItem(i, stacks.get(i).stack.copyWithCount(1));
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
        for (int i = 0, size = ghostInventory.getContainerSize(), listSize = stacks.size(); i < size; i++) {
            ItemStack stackInSlot = ghostInventory.getItem(i);
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
