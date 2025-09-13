package com.zurrtum.create.content.logistics.filter;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllMenuTypes;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

public class PackageFilterMenu extends AbstractFilterMenu {

    public String address;

    public PackageFilterMenu(int id, PlayerInventory inv, ItemStack stack) {
        super(AllMenuTypes.PACKAGE_FILTER, id, inv, stack);
    }

    @Override
    protected int getPlayerInventoryXOffset() {
        return 40;
    }

    @Override
    protected int getPlayerInventoryYOffset() {
        return 101;
    }

    @Override
    protected void addFilterSlots() {
    }

    @Override
    protected ItemStackHandler createGhostInventory() {
        return new ItemStackHandler();
    }

    @Override
    public void clearContents() {
        address = "";
    }

    @Override
    protected void initAndReadInventory(ItemStack filterItem) {
        super.initAndReadInventory(filterItem);
        address = filterItem.getOrDefault(AllDataComponents.PACKAGE_ADDRESS, "");
    }

    @Override
    protected void saveData(ItemStack filterItem) {
        super.saveData(filterItem);
        if (address.isBlank())
            filterItem.remove(AllDataComponents.PACKAGE_ADDRESS);
        else
            filterItem.set(AllDataComponents.PACKAGE_ADDRESS, address);
    }

    @Override
    public ItemStack quickMove(PlayerEntity playerIn, int index) {
        return ItemStack.EMPTY;
    }

}