package com.zurrtum.create.api.contraption.storage.item.menu;

import com.zurrtum.create.infrastructure.items.ItemInventory;
import net.minecraft.entity.ContainerUser;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class StorageInteractionWrapper implements ItemInventory {
    private final Inventory inv;
    private final Predicate<PlayerEntity> stillValid;
    private final Consumer<ContainerUser> onClose;

    public StorageInteractionWrapper(Inventory inv, Predicate<PlayerEntity> stillValid, Consumer<ContainerUser> onClose) {
        this.inv = inv;
        this.stillValid = stillValid;
        this.onClose = onClose;
    }

    @Override
    public int size() {
        return inv.size();
    }

    @Override
    public ItemStack getStack(int slot) {
        return inv.getStack(slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        inv.setStack(slot, stack);
    }

    @Override
    public int getMaxCountPerStack() {
        return inv.getMaxCountPerStack();
    }

    @Override
    public int getMaxCount(ItemStack stack) {
        return inv.getMaxCount(stack);
    }

    @Override
    public int insert(ItemStack stack) {
        return inv.insert(stack);
    }

    @Override
    public int extract(ItemStack stack) {
        return inv.extract(stack);
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return stillValid.test(player);
    }

    @Override
    public void onClose(ContainerUser player) {
        onClose.accept(player);
    }
}
